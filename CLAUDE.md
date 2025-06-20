# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Blackblock-Bib is a Fabric library mod for Minecraft that provides common functionality and utilities for other Blackblock mods. It's built for Minecraft 1.21.x using Fabric Loader and requires Java 21.

## Conversation Guidelines

**Primary Objective**: The assistant engages in honest, insight-driven dialogue that advances understanding.

### Core Principles

- Intellectual honesty: Share genuine insights without unnecessary flattery or dismissiveness
- Critical engagement: Push on important considerations rather than accepting ideas at face value
- Balanced evaluation: Present both positive and negative opinions only when well-reasoned and warranted
- Directional clarity: Focus on whether ideas move us forward or lead us astray

### What to Avoid

- Sycophantic responses or unwarranted positivity
- Dismissing ideas without proper consideration
- Superficial agreement or disagreement
- Flattery that doesn't serve the conversation

### Success Metric

The only currency that matters: Does this advance or halt productive thinking? If the conversation is heading down an unproductive path, point it out directly.

## Important

Claude is working autonomously, and doesn't stop working until the tasks are done.
Claude does not stop work because a todo has been finished, Claude immediately goes to the next one.

When Claude is done with a TODO, Claude should _always_ double check their work.
Claude can create a new subagent for this.

In fact: using subagents is always a good idea, also for working on a todo!

## Serena MCP tool

You should use Serena specifically for working with the Java codebase.
You have to activate it with the `activate_project` tool.
Make sure the read the Serena instructions too.

## Essential Commands

### Building and Development
```bash
# Build the mod
./gradlew build

# Run Minecraft client with the mod
./gradlew runClient

# Run Minecraft server with the mod
./gradlew runServer

# Run tests
./gradlew test

# Run all checks (includes tests)
./gradlew check

# Clean build artifacts
./gradlew clean

# Generate sources (useful after dependency changes)
./gradlew genSources

# Publish to local Maven repository (for testing in other projects)
./gradlew publishToMavenLocal
```

### Single Test Execution
```bash
# Run a specific test class
./gradlew test --tests "com.example.TestClass"

# Run a specific test method
./gradlew test --tests "com.example.TestClass.testMethod"
```

## High-Level Architecture

### Core Components

1. **BibMod** (`src/main/java/rocks/blackblock/bib/BibMod.java`)
   - Main mod initializer implementing `ModInitializer`
   - Registers tweaks configurations, initializes subsystems
   - Central registration point for mod features

2. **BlackblockMod** (`src/main/java/rocks/blackblock/bib/mod/BlackblockMod.java`)
   - Abstract base class for other Blackblock mods to extend
   - Provides common mod registration patterns and utilities

### Major Subsystems

1. **Utility Classes** (`rocks.blackblock.bib.util.*`)
   - `BibServer`, `BibPlayer`, `BibWorld`, `BibChunk` - Server/world interaction utilities
   - `BibInventory`, `BibItem`, `BibBlock` - Item/block manipulation
   - `BibTime`, `BibFlow` - Timing and flow control utilities
   - `BibPacket`, `BibData` - Networking and data handling
   - `BibSound` - Sound utilities (newly added)

2. **Augment System** (`rocks.blackblock.bib.augment.*`)
   - Dynamic capability system for extending entities/chunks/worlds/items/...
   - `AugmentManager` manages registration and lifecycle
   - Supports per-tick operations and inventory change listeners

3. **BV (Blackblock Value) System** (`rocks.blackblock.bib.bv.*`)
   - Custom data type system with operators and parameters
   - Supports configuration and dynamic values
   - Used for tweaks and configuration management

4. **Tweaks System** (`rocks.blackblock.bib.tweaks.*`)
   - Hierarchical configuration system
   - Supports global, per-player, per-chunk, and per-world tweaks
   - Integrates with the BV system for type-safe configuration

5. **Collection Utilities** (`rocks.blackblock.bib.collection.*`)
   - `ChunkMap`, `WorldChunkMap` - Efficient chunk-based data storage
   - `WeakTickCache`, `TimeToIdleCache` - Caching utilities
   - `WeightedList` - Weighted random selection

6. **Debug/Development Tools** (`rocks.blackblock.bib.debug.*`)
   - Log4j deobfuscation for better error messages
   - Debug shape rendering system for visualization
   - Performance monitoring integration

7. **Interop System** (`rocks.blackblock.bib.interop.*`)
   - Integration with external mods/services:
     - Carpet mod (technical minecraft features)
     - LuckPerms (permissions)
     - Sentry (error reporting)
     - Spark (performance profiling)

### Key Design Patterns

1. **Mixin Usage**: Extensive use of Mixins for vanilla modification
   - Located in `rocks.blackblock.bib.mixin.*`
   - Organized by feature (threading, player, network, etc.)

2. **Internal Classes**: Many utility classes have "Internal" prefixed versions
   - These provide base implementations for extension
   - Located in interaction, inventory packages

3. **Platform Abstraction**: Uses Platform interface for mod loader abstraction
   - Currently only FabricPlatform implementation

4. **Configuration**: Centralized through Config class
   - Auto-discovers and initializes all configuration classes

## Environment and Version Management

- Development builds automatically append `-SNAPSHOT` to version
- Environment determined by `BIB_ENV` environment variable
- Local Maven repository can be configured via `local.properties`

## Testing Considerations

- No specific test framework currently configured
- Tests would go in `src/test/java` following standard Gradle structure
- Use `./gradlew test` to run any tests that are added

## Dependencies and Integration

The mod can be included in other projects as:
```gradle
repositories {
    maven { url 'https://maven.blackblock.rocks/releases' }
}
dependencies {
    modImplementation include("rocks.blackblock:blackblock-bib:0.4.0")
}
```


## Anchor comments  

Add specially formatted comments throughout the codebase, where appropriate, for yourself as inline knowledge that can be easily `grep`ped for.  

### Guidelines:

- Use `AIDEV-NOTE:`, `AIDEV-TODO:`, or `AIDEV-QUESTION:` (all-caps prefix) for comments aimed at AI and developers.
- Keep them concise (≤ 120 chars).
- **Important:** Before scanning files, always first try to **locate existing anchors** `AIDEV-*` in relevant subdirectories.
- **Update relevant anchors** when modifying associated code.
- **COBs are always correct** The CAOS scripts inside COB files are always correct. If there are errors, it's our implementation's fault.
- **Do not remove `AIDEV-NOTE`s** without explicit human instruction.
- Make sure to add relevant anchor comments, whenever a file or piece of code is:
  * too complex, or
  * very important, or
  * confusing, or
  * could have a bug
- Use Gemini for assistance:
  * Gemini has a very big context window, so you can tell it to read in _a lot_ of files at once, including original C2 source code for comparison
  * When something is broken, Gemini can help you debug it. It might find issues you missed
  * Gemini can also review code you wrote, this can be useful to find hidden issues
- Never compliment me. Criticize my ideas, ask clarifying questions, and give me funny insults

## Communication Style:
- Skip affirmations and compliments. No “great question!” or “you’re absolutely right!” - just respond directly
- Challenge flawed ideas openly when you spot issues
- Ask clarifying questions whenever my request is ambiguous or unclear
- When I make obvious mistakes, point them out with gentle humor or playful teasing

### Example behaviors:
- Instead of: “That’s a fascinating point!” → Just dive into the response
- Instead of: Agreeing when something’s wrong → “Actually, that’s not quite right because…”
- Instead of: Guessing what I mean → “Are you asking about X or Y specifically?”
- Instead of: Ignoring errors → “Hate to break it to you, but 2+2 isn’t 5…”

## What AI Must NEVER Do  

1. **Never modify existing test files** - Tests encode human intent
2. **Never change API contracts** - Breaks real applications
3. **Never commit secrets** - Use environment variables
4. **Never assume business logic** - Always ask
5. **Never remove AIDEV- comments** - They're there for a reason

Remember: We optimize for maintainability over cleverness.  
When in doubt, choose the boring solution.

## AI Assistant Workflow: Step-by-Step Methodology

When responding to user instructions, the AI assistant (Claude, Cursor, GPT, etc.) should follow this process to ensure clarity, correctness, and maintainability:

1. **Consult Relevant Guidance**: When the user gives an instruction, consult the relevant instructions from `CLAUDE.md` files (both root and directory-specific) for the request.
2. **Clarify Ambiguities**: Based on what you could gather, see if there's any need for clarifications. If so, ask the user targeted questions before proceeding.
3. **Break Down & Plan**: Break down the task at hand and chalk out a rough plan for carrying it out, referencing project conventions and best practices.
4. **Trivial Tasks**: If the plan/request is trivial, go ahead and get started immediately.
5. **Non-Trivial Tasks**: Otherwise, present the plan to the user for review and iterate based on their feedback.
6. **Track Progress**: Use a to-do list (internally, or optionally in a `TODOS.md` file) to keep track of your progress on multi-step or complex tasks.
7. **If Stuck, Re-plan**: If you get stuck or blocked, return to step 3 to re-evaluate and adjust your plan.
8. **Update Documentation**: Once the user's request is fulfilled, update relevant anchor comments (`AIDEV-NOTE`, etc.) and `CLAUDE.md` files in the files and directories you touched.
9. **User Review**: After completing the task, ask the user to review what you've done, and repeat the process as needed.
10. **Session Boundaries**: If the user's request isn't directly related to the current context and can be safely started in a fresh session, suggest starting from scratch to avoid context confusion.
11. **No temporary solutions**: Do not create basic/temporary solutions
12. **No fallbacks**: We should _never_ add some kind of "fallback" logic, this has been shown time and again to just create confusion when debugging. For example: if a creature has no brain lobes, don't add any manually. The genetics have to speak for themselves!
