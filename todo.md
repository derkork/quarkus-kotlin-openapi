## Config

- [ ] Endpunkte anhand der OpenAPI-Tags oder Konfigurationswerte in getrennte Server/Client-Interfaces splitten
- [ ] Name für Klassen per Konfiguration ändern (z.B. um den erzeugten NAmen für inline-Klassen zu ändern, oder Namenskonflikte zu vermeiden) 
- [ ] Operation-Id per Konfiguration änderbar (falls man mit einer fremden API arbeiten muß)
- [ ] Validierung für Klassen änderbar/ergänzbar

## Parser
               
- [ ] Referenzen (z.B. auf Schema) können Werte überschreiben (z.B. Nullable, Default-Wert, Validierung, etc). Siehe dazu auch https://github.com/OAI/OpenAPI-Specification/issues/1368
- [ ] Security
- [ ] Tags
- [ ] bessere Fehlerbehandlung im Parser
- [ ] die verschiedenen OpenAPI Versionen unterstützen
- [ ] Formate wie date, date-time, uuid unterstützen
- [ ] Mixed-Datentypen
  ```yaml
  oneOf:
  - type: string
  - type: integer
  ```
- [ ] `nullable` Datentypen
  ```yaml  
  "foo":
    # OAS 3.1
    type: [string, 'null']

    # OAS 3.0
    # type: string
    # nullable: true
  ```
- [ ] Default-Werte für optionale Parameter und Properties
- [ ] style und explode für Array Parameter
- [ ] Default Responses
- [ ] Referenz auf Response-Definition
- [ ] Read-Only und Write-Only Properties
  ```yaml  
  "foo":
    type: object
    properties:
      id:
        # Returned by GET, not used in POST/PUT/PATCH
        type: integer
        readOnly: true
      username:
        type: string
      password:
        # Used in POST/PUT/PATCH, not returned by GET
        type: string
        writeOnly: true
  ```
- [ ] Free-Form Objekte
  ```yaml
  "foo":
    type: object
  
    # This is equivalent to
    # type: object
    # additionalProperties: true
  
    # and equivalent to
    # type: object
    # additionalProperties: {}
  ```
- [ ] Parameter auf Pfad-Ebene die für alle Methoden gültig sind
  ```yaml
  paths:
  /user/{id}:
    parameters:
      - in: path
        name: id
        schema:
          type: integer
        required: true
        description: The user ID
    get:
      summary: Gets a user by ID
      ...
    patch:
      summary: Updates an existing user with the specified ID
      ...
    delete:
      summary: Deletes the user with the specified ID
      ...
  ```
- [ ] Referenz auf gemeinsame Parameter unterhalb von `/components/parameters`
- [ ] weitere Definitionen unterhalb von `/components` https://swagger.io/docs/specification/components/
- [ ] allOf, oneOf und anyOf Besonderheiten (unterschiedlich für Enums, primitive Typen und Mischformen)
  ```yaml
  - name: genre
    in: query
    schema:
      allOf:
        - $ref: "#/components/schemas/Genre"
        - default: crime  # ändert den default
          enum:           # fügt neue Werte zur enum hinzu 
            - foo
  ```
 
## Generator
                
- [ ] referenzen auf referenzen
- [ ] Consumes/Produces Annotation für verschiedene Datenformate (Json, Xml, Binär)
- [ ] @ClientHeaderParam im Client Interface
- [ ] Zugriff auf Request-Header und setzen von Response-Header
- [ ] Zugriff auf Cookies



