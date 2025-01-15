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





neue todos:
- unique names
- model prefix/postfix
- test schreiben für den ParameterSchemaPatch, ob er in allen Fällen richtig funktioniert und Fehlermeldung verbessern, wenn parameters unterhalb einer operation kein array ist
- rekursion und überschreiben von objekt-parametern bei rekursiven und normalen objekten
- number konvertierung bei double min/max 
- nicht alle modelle für alle content types zulassen
- test für responsewithinterface schreiben
- kotlin-BuildMEthode wie kotlinClass sollten neues objekt liefern
- contentType.matches durch ContentInfo ersetzen
- test für umbenennung von objekten/properties (fehlermeldungen etc)
- default handler implementierungen nehmen mal modelusage und mal typeref
- context für Maybe prüfen, ob überall richtiger wert verwendet wird
- context prüfen, ob variableNameOf verwendet wird

class NameRegistry {

    private val nameBuilder = mutableMapOf<String, NameBuilder>()

    fun uniqueNameFor(name: ClassName, shared: Boolean = false): ClassName {
        val builder = nameBuilder.getOrPut(name.value) { NameBuilder() }
        return when (shared) {
            true -> builder.shared(name)
            false -> builder.next(name)
        }
    }

     private class NameBuilder {

        private var nextIndex = -1
        private var shared = -1

        fun next(name: ClassName) = foo(name, ++nextIndex)

        fun shared(name: ClassName): ClassName {
            if (shared == -1) {
                // it's the first time, we need the shared class name. freeze the index and reuse it from now on
                shared = ++nextIndex
            }
            return foo(name, shared)
        }

        private fun foo(name: ClassName, index: Int): ClassName {
            if (index == 0) {
                return name
            }
            return name.extend(postfix = "$index")
        }

    }
}