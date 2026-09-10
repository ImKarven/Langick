# Langick
Langick is a library for resolving Minecraft translation keys to their strings in the respective language and Minecraft version.

> [!NOTE]
> Langick is not available to be installed yet. It is still under development!

# Example

## Code
```java
final Langick langick = new Langick();
final VersionedLanguage englishLanguage = new VersionedLanguage("26.2", "en_us");
final VersionedLanguage spanishLanguage = new VersionedLanguage("26.2", "es_es");
final VersionedLanguage japaneseLanguage = new VersionedLanguage("26.2", "ja_jp");
langick.downloadLanguage(englishLanguage);
langick.downloadLanguage(spanishLanguage);
langick.downloadLanguage(japaneseLanguage);

try {
    final String englishText = langick.resolveTranslation(englishLanguage, "options.language.title");
    final String spanishText = langick.resolveTranslation(spanishLanguage, "options.language.title");
    final String japaneseText = langick.resolveTranslation(japaneseLanguage, "options.language.title");

    System.out.println("English: " + englishText);
    System.out.println("Spanish: " + spanishText);
    System.out.println("Japanese: " + japaneseText);

} catch (final InvalidLanguageException | InvalidTranslationKeyException exception) {
    throw new RuntimeException(exception);
}
```

## Output
```
English: Language
Spanish: Idioma
Japanese: 言語設定
```
