- `description` everywhere if we want to include them in the generated sources
- `deprecated` everywhere if we want to include them in the generated sources
- map-support
- split schemas for readonly/writeonly
- unique names

- x-model-name und x-container-model-name sollten als rawClassName verwendet werden (wie bereits bei x-enum-item-names) und sollten bei der UniqueName berechnung zuerst betrachtet werden

- nullable aber required felder müssen im request enthalten sein
- nullable felder mit default, trotzdem nullable?
- validation
- nullable/required for form-parameter!? wenn objekt optional aber property required?