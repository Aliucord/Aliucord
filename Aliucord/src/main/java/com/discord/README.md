# Discord Stubs

Stubs for Discord's classes.

This is only necessary for specific classes that are converted to Java ASM via dex2jar,
specifically ones that have had their `InnerClass`/`EnclosingClass`/`EnclosingMethod` attributes
stripped to make them referencable for patches.

However, for specific anonymous classes that contain `$` characters, but not two of them sequentially like `$$`,
javac and kotlinc both think that this is an inner class anyways, and break decompilation and references to those classes from other
decompiled classes. This is when stubs are needed.

Eventually this could be fixed for Kotlin with the use of extra class metadata, but Java will always behave like this.
