-dontoptimize
-dontobfuscate
-dontpreverify
-dontwarn scala.**
-keep class scala.collection.SeqLike {
    public protected *;
}
-ignorewarnings
