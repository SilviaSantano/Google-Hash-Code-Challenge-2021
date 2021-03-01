class Street(
    val id: Int,
    val name: String,
    val start: Intersection,
    val end: Intersection,
    val time: Int
) {
    var load: Int = 0
    var averageLoad: Int = 0
}