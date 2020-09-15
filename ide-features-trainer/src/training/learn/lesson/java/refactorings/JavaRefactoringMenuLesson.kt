// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package training.learn.lesson.java.refactorings

import com.intellij.CommonBundle
import com.intellij.refactoring.RefactoringBundle
import com.intellij.testGuiFramework.framework.GuiTestUtil
import com.intellij.testGuiFramework.util.Key
import com.intellij.util.ui.UIUtil
import training.commands.kotlin.TaskRuntimeContext
import training.learn.LessonsBundle
import training.learn.interfaces.Module
import training.learn.lesson.general.refactorings.RefactoringMenuLessonBase
import training.learn.lesson.kimpl.LessonContext
import training.learn.lesson.kimpl.LessonUtil
import training.learn.lesson.kimpl.parseLessonSample
import javax.swing.JDialog

class JavaRefactoringMenuLesson(module: Module) : RefactoringMenuLessonBase("java.refactoring.menu", module, "JAVA") {
  private val sample = parseLessonSample("""
    import java.io.BufferedReader;
    import java.io.FileReader;
    import java.io.IOException;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.stream.Collectors;
    
    class Refactorings {
        public static void main(String[] args) throws IOException {
            List<String> array = readStrings();
            List<String> filtered = array.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
            for (String s : filtered) {
                System.out.println(s);
            }
        }

        private static List<String> readStrings() throws IOException {
            try(BufferedReader reader = new BufferedReader(<select>new FileReader("input.txt")</select>)) {
                ArrayList<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                return lines;
            }
        }
    }      
  """.trimIndent())

  override val lessonContent: LessonContext.() -> Unit = {
    prepareSample(sample)
    extractParameterTasks()
    moreRefactoringsTasks()
  }

  private fun LessonContext.moreRefactoringsTasks() {
    waitBeforeContinue(300)

    val inlineVariableName = "array"

    caret(inlineVariableName)

    actionTask("Inline") {
      LessonsBundle.message("java.refactoring.menu.inline.variable",
                            code(inlineVariableName), action("Refactorings.QuickListPopupAction"), action(it))
    }
    task {
      stateCheck {
        !editor.document.charsSequence.contains(inlineVariableName)
      }
    }

    caret("txt", true)

    actionTask("IntroduceConstant") {
      LessonsBundle.message("java.refactoring.menu.introduce.constant",
                            action("Refactorings.QuickListPopupAction"), action(it))
    }
    task {
      stateCheck {
        extractConstantDialogShowing()
      }
    }
    task {
      text(LessonsBundle.message("java.refactoring.menu.confirm.constant",
                                 LessonUtil.rawEnter(), strong(CommonBundle.getOkButtonText())))
      stateCheck {
        !extractConstantDialogShowing()
      }
      test {
        GuiTestUtil.shortcut(Key.ENTER)
      }
    }
  }

  private fun TaskRuntimeContext.extractConstantDialogShowing() =
    focusOwner?.let { fo -> UIUtil.getParentOfType(JDialog::class.java, fo) }?.title ==
      RefactoringBundle.message("introduce.constant.title")
}
