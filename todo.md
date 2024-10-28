- `description` everywhere if we want to include them in the generated sources
- `deprecated` everywhere if we want to include them in the generated sources
           
- query und header paramater mit listen sollten per default nicht nullable sein, weil das framework eh ne leere liste liefert. bei headern im response das gleiche
- x-model-name und x-container-model-name sollten als rawClassName verwendet werden (wie bereits bei x-enum-item-names) und sollten bei der UniqueName berechnung zuerst betrachtet werden

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