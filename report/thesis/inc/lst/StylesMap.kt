@Document(collection = "styles_map")
data class StylesMap(
    @Id
    var id: String? = null,
    var links: List<StyleLink>? = null
)

data class StyleLink(
    var elementId: String? = null,
    var styleId: String? = null,
    var scope: String? = null,
    var state: String? = null
)
