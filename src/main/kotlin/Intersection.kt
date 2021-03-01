
class Intersection(val id: Int) {
    var incoming: List<Street> = listOf()
    var outgoing: List<Street> = listOf()
    var load: Int = 0
}