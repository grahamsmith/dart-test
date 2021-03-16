# dart-test

![Build](https://github.com/grahamsmith/dart-test/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/16299.svg)](https://plugins.jetbrains.com/plugin/16299)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/16299.svg)](https://plugins.jetbrains.com/plugin/16299)

## Description
<!-- Plugin description -->
This plugin addresses missing functionality when working with Dart/Flutter, specifically the pain of creating tests in a package structure that reflects the implementation code.

Features:

- alt+enter on a class name to generate a test inside the tests folder following the same relative path.
- Right click on a dart file to create a test file, via the new file menu, in the test folder in the same relative location.
- Right click inside the test folder to create a test in that location.
- All tests come with the test package as imported and the bare bones to create a test.

Create a Dart Test file from the "New Item" menu, give it a name and watch it *magically* create the test in the right folder .

![Create a Dart Test file from source or test folders](https://github.com/grahamsmith/dart-test/blob/main/images/new_dart_file.png?raw=true)

All tests are generated ready to go with the basic elements filled in. Groups and Widget tests coming soon!

![Create a Dart Test file from source or test folders](https://github.com/grahamsmith/dart-test/blob/main/images/unit_test_template.png?raw=true)


### Future work

- Create other types of test e.g. widget and test groups.
- Allow the path to unit, widget and integration tests to be configurable.

<!-- Plugin description end -->

## Installation

- Using IDE built-in plugin system:
  
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "dart-test"</kbd> >
  <kbd>Install Plugin</kbd>
  
- Manually:

  Download the [latest release](https://github.com/grahamsmith/dart-test/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>


---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
