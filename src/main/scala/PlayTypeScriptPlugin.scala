package com.github.hexx

import sbt._
import sbt.Keys._

object PlayTypeScriptPlugin extends Plugin {
  val typescriptEntryPoints = SettingKey[PathFinder]("play-typescript-entry-points")
  val typescriptOptions = SettingKey[Seq[String]]("play-typescript-options")

  val TypeScriptCompiler = PlayProject.AssetsCompiler("ts",
    (_ ** "*.ts"),
    typescriptEntryPoints,
    { (name, min) => name.replace(".ts", if (min) ".min.js" else ".js") },
    { (typescriptFile, options) => com.github.hexx.TypeScriptCompiler.compile(typescriptFile, options) },
    typescriptOptions
  )

  override val settings = Seq(
    typescriptEntryPoints <<= (sourceDirectory in Compile)(_ / "assets" ** "*.ts"),
    typescriptOptions := Seq.empty[String],
    resourceGenerators in Compile <+= TypeScriptCompiler
  )
}
