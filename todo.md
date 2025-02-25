- `description` everywhere if we want to include them in the generated sources
- `deprecated` everywhere if we want to include them in the generated sources
           
- nullable aber required felder müssen im request enthalten sein
- nullable/required for form-parameter!? wenn objekt optional aber property required?
- generische typen im custom type mapping

- cache für $ref auf Parameter, Responses, Bodies, damit inline-schemas in geteilten responses nicht zu doppelten model-klassen führen

bessere Fehlermeldungen
- und bei probable bug

# Beschreibung

- x-constraints
- x-model-name
- x-container-model-name
- x-enum-item-names
- x-generic-response-name





neue todos:
DependencyVogel umbenennen

- test schreiben für den ParameterSchemaPatch, ob er in allen Fällen richtig funktioniert und Fehlermeldung verbessern, wenn parameters unterhalb einer operation kein array ist
- rekursion und überschreiben von objekt-parametern bei rekursiven und normalen objekten
- nicht alle modelle für alle content types zulassen
- test für responsewithinterface schreiben
- contentType.matches durch ContentInfo ersetzen
- test für umbenennung von objekten/properties (fehlermeldungen etc)
- default handler implementierungen nehmen mal modelusage und mal typeref
- isOkResponse bei Fehler bessere infos liefern     (z.b. meldungen aus responseError)

