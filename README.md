# Kotlin Bracket Converter
intellij-kotlin-bracket-converter

![Build](https://github.com/No3x/intellij-kotlin-bracket-converter/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

Light-weight IntelliJ IDEA/Android Studio plugin that lets you flip Kotlin brackets in place. It adds a single intention action that can turn a lambda written with braces into parentheses (and back again) right where your caret sits.

<!-- Plugin description -->
Kotlin Bracket Converter adds an intention action for quickly swapping Kotlin braces and parentheses at the caret.
- Put the caret on the opening `{` of a lambda literal to turn the braces into `(` `)` in place.
- Put the caret on an opening `(` to turn that pair into `{` `}`.

Use it to prototype DSL-style call sites, experiment with code style, or simply fix the occasional misplaced bracket without re-typing the line. The action is intentionally simple: it only swaps the two characters; review the result and reformat if needed.
<!-- Plugin description end -->

### Example
Before:
```kotlin
listOf(1, 2).map { it * 2 }
```
After running **Convert { } to ( )** with the caret on the `{`:
```kotlin
listOf(1, 2).map ( it * 2 )
```
You can invoke the opposite intention on the `(` to revert back to braces.

## How to use
- Open a Kotlin file and place the caret on the opening `{` of a lambda expression or on an opening `(` you want to swap.
- Press `Alt+Enter` (or your intention shortcut) and choose **Convert { } to ( )** or **Convert ( ) to { }**.
- The plugin replaces the matching pair directly in the editor; run code formatting if spacing looks off.

## Why it saves time
- Swaps both ends of the bracket pair in one action—no hunting for the matching bracket or retyping.
- Keeps you in flow: `Alt+Enter` beats multiple cursor moves, edits, and reformat steps.
- Great for quick style experiments (DSLs, lambdas) and for undoing a bracket style you no longer want.

## Installation
- ~~IDE Marketplace: `Settings/Preferences` > `Plugins` > `Marketplace` > search for "kotlin-bracket-converter" > Install.~~
- ~~JetBrains Marketplace page: https://plugins.jetbrains.com/plugin/MARKETPLACE_ID (replace `MARKETPLACE_ID` once published).~~
- Manual: Download the latest release from GitHub ~~or Marketplace~~ and choose `Install plugin from disk...`.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
