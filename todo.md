- `description` everywhere if we want to include them in the generated sources
- `deprecated` everywhere if we want to include them in the generated sources
- map-support
           
- query und header paramater mit listen sollten per default nicht nullable sein, weil das framework eh ne leere liste liefert. bei headern im response das gleiche

- x-model-name und x-container-model-name sollten als rawClassName verwendet werden (wie bereits bei x-enum-item-names) und sollten bei der UniqueName berechnung zuerst betrachtet werden

- nullable aber required felder müssen im request enthalten sein
- nullable felder mit default, trotzdem nullable?
- nullable/required for form-parameter!? wenn objekt optional aber property required?


- literal Funktionen für BigInteger, BigDecimal


# Beschreibung

- x-constraints
- x-model-name
- x-container-model-name
- x-enum-item-names
- 