package com.github.oconnormi.ddf.replicator

import static groovy.io.FileType.DIRECTORIES
import static groovy.io.FileType.FILES
import static java.nio.file.Files.copy
import static java.nio.file.Files.createDirectories
import static java.nio.file.Files.exists
import static java.nio.file.Paths.get
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING
import static org.ho.yaml.Yaml.load

def replicate(configFile, outputDir, src) {
  println "Config File: $configFile"
  println "Output Directory: $outputDir"
  println "Src directory: $src"

  if (!exists(configFile)) {
    println "Config File doesn't exist!"
    System.exit(1)
  }

  if (!exists(src)) {
    println "Source directory doesn't exist"
    System.exit(1)
  }

  if (!exists(outputDir)) {
    println "Creating output directory : $outputDir"
    createDirectories(outputDir)
  }

  config = load(configFile.toFile())

  for (node in config) {
    nodeName = node.key
    nodeValue = node.value
    nodeDir = get(outputDir.toString(), nodeName)
    // Isolate the nested Maps
    configList = nodeValue.configs
    sysProps = nodeValue.system
    includeList = nodeValue.includes
    appList = nodeValue.applications

    println "Creating instance: $nodeName, located in: $nodeDir"
    // Copy All Directories from source
    src.toFile().traverse(type: DIRECTORIES) { it ->
      targetPath = nodeDir.resolve(src.relativize(it.toPath()))
      if (!exists(targetPath)) { createDirectories(targetPath) }
    }
    // Copy all files from source
    src.toFile().traverse(type: FILES) { it -> copy(it.toPath(), nodeDir.resolve(src.relativize(it.toPath()))) }

    createConfigs(nodeName, nodeDir, configList)
    updateSystem(nodeName, nodeDir, sysProps)
    getIncludes(nodeDir, includeList)
    setApps(nodeDir, appList)
  }

}

// Update all configs
def createConfigs(nodeName, nodeDir, configList) {
  for (config in configList) {
    file = new File(nodeDir.toString(), config.key)
    props = config.value
    println "Updating config: $file for: $nodeName"
    updatePropFile(file, props)
  }
}

def updateSystem(nodeName, nodeDir, sysProps) {
  println "Updating System Properties for: $nodeName"
  file = new File(nodeDir.toString(), "etc/system.properties")
  updatePropFile(file, sysProps)
}

// Copy all includes into import destination
def getIncludes(nodeDir, includeList) {
  for (include in includeList) {
    includeSrc = get(include.src)
    includeDest = get(nodeDir.toString(), include.dest)
    if (exists(includeSrc)) {
      println "Including: $includeSrc -> $includeDest"
      copy(includeSrc, includeDest, REPLACE_EXISTING)
    }
    else {
      println "Include does not exist: $includeSrc"
    }
  }
}

def setApps(nodeDir, appList) {
  file = new File(nodeDir.toString(), "etc/org.codice.ddf.admin.applicationlist.properties")
  println "Updating Apps for: $nodeName"
  updatePropFile(file, appList)
}

def updatePropFile(file, props) {
  if (!file.exists()) {
    file.createNewFile()
  }
  propsFile = new Properties()
  file.withInputStream { propsFile.load(it) }
  for (prop in props) {
    println "Setting property: $prop.key : $prop.value"
    propsFile.setProperty(prop.key, prop.value)
  }
  propsFile.store(new FileOutputStream(file), null)
}

def run(args) {
  def cli = new CliBuilder(usage: 'ddf-replicator -[cho] [src]')
  // Create the list of options.
  cli.with {
    h longOpt: 'help', 'Show usage information'
    c longOpt: 'config-path', args: 1, argName: 'config', 'Path to config.yaml, defaults to working directory'
    o longOpt: 'output-dir', args: 1, argName: 'output', 'Path to output directory, defaults to "${PWD}/out"'
  }

  // Show Usage when -h or --help is specified, or if no arguments are given
  def options = cli.parse(args)

  def arguments = options.arguments()
  if (arguments.size != 1 || options.h) {
  cli.usage()
  return
  }

  def configFile
  def outputDir
  def cwd = System.getProperty("user.dir")
  def src = get(arguments[0])

  if (!options.c) {
    configFile = get(cwd, "config.yaml")
  }
  else {
    configFile = get(options.c)
  }
  if (!options.o) {
    outputDir = get(cwd, "out")
  }
  else {
    outputDir = get(options.o)
  }

  replicate(configFile, outputDir, src)
}

run(args)
