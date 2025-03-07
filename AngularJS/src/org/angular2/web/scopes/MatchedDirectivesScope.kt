// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.web.scopes

import com.intellij.model.Pointer
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.xml.XmlTag
import com.intellij.refactoring.suggested.createSmartPointer
import com.intellij.webSymbols.WebSymbol
import com.intellij.webSymbols.WebSymbolsScopeWithCache
import org.angular2.Angular2Framework
import org.angular2.codeInsight.Angular2LibrariesHacks
import org.angular2.codeInsight.attributes.Angular2ApplicableDirectivesProvider
import org.angular2.codeInsight.template.Angular2TemplateElementsScopeProvider.Companion.isTemplateTag
import org.angular2.entities.Angular2Directive
import org.angular2.web.Angular2DirectiveSymbolWrapper
import org.angular2.web.Angular2Symbol

class MatchedDirectivesScope(tag: XmlTag)
  : WebSymbolsScopeWithCache<XmlTag, Unit>(Angular2Framework.ID, tag.project, tag, Unit) {

  override fun createPointer(): Pointer<MatchedDirectivesScope> {
    val tag = dataHolder.createSmartPointer()
    return Pointer {
      tag.dereference()?.let { MatchedDirectivesScope(it) }
    }
  }

  override fun initialize(consumer: (WebSymbol) -> Unit, cacheDependencies: MutableSet<Any>) {
    cacheDependencies.add(PsiModificationTracker.MODIFICATION_COUNT)
    Angular2ApplicableDirectivesProvider(dataHolder)
      .matched.forEach { directive ->
        directive.exportAs.forEach { consumer(it.value) }
        collectSymbols(directive) { symbol ->
          consumer(Angular2DirectiveSymbolWrapper.create(directive, symbol, WebSymbol.Priority.HIGHEST))
        }
      }
  }

  private fun collectSymbols(directive: Angular2Directive, consumer: (Angular2Symbol) -> Unit) {
    if (!directive.directiveKind.isRegular && !isTemplateTag(dataHolder)) {
      return
    }

    directive.inOuts.forEach(consumer)
    directive.inputs.forEach(consumer)
    directive.outputs.forEach(consumer)
    directive.attributes.forEach(consumer)

    Angular2LibrariesHacks.hackIonicComponentAttributeNames(directive).forEach(consumer)
  }

}