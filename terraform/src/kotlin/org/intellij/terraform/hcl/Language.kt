// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hcl

import com.intellij.lang.Language

object HCLLanguage : Language("HCL") {
  override fun isCaseSensitive() = true
  override fun getDisplayName(): String {
    return "HashiCorp Configuration Language"
  }
}

