import java.io.File

class CityPlan(inputFile: String, private val outputFile: String) {
    private val D: Int
    private val I: Int
    private val S: Int
    private val V: Int
    private val F: Int
    private val streets: MutableList<Street>
    private var cars: MutableList<Car>
    private val intersections: MutableList<Intersection>
    private val schedule: MutableList<IntersectionSchedule>

    init {
        // Parse input data
        var index = 0
        val lines = File(inputFile).readLines()
        fun parseLineAsList(): MutableList<String> = lines[index++].split(' ').toMutableList()
        val firstLine = parseLineAsList()
        D = firstLine[0].toInt()
        I = firstLine[1].toInt()
        S = firstLine[2].toInt()
        V = firstLine[3].toInt()
        F = firstLine[4].toInt()
        println("Duration: $D seconds")
        println("Intersections: $I")
        println("Streets: $S")
        println("Cars: $V")
        println("Bonus per car: $F\n")

        // Parse Streets
        streets = mutableListOf()
        for (s in 0 until S) {
            val street = parseLineAsList()
            streets.add(
                Street(
                    s,
                    street[2],
                    Intersection(street[0].toInt()),
                    Intersection(street[1].toInt()),
                    street[3].toInt()
                )
            )
        }

        // Parse Cars
        cars = mutableListOf()
        for (c in 0 until V) {
            val car = parseLineAsList()
            cars.add(Car(c, car[0].toInt(), car.subList(1, car[0].toInt() + 1)))
        }

        // Define Intersections with their incoming and outgoing streets
        intersections = getIntersections()

        // Init Schedule
        schedule = mutableListOf()
    }

    private fun getIntersections(): MutableList<Intersection> {
        val intersections = mutableListOf<Intersection>()
        for (i in 0 until I) {
            val inter = Intersection(i)
            inter.incoming = getIncomingStreets(i)
            inter.outgoing = getOutgoingStreets(i)
            intersections.add(inter)
        }
        return intersections
    }

    private fun getIncomingStreets(i: Int): List<Street> {
        return streets.filter { it.end.id == i }
    }

    private fun getOutgoingStreets(i: Int): List<Street> {
        return streets.filter { it.start.id == i }
    }

    private fun computeTravelTimes() {
        cars.map { car ->
            car.travelTime = car.path.subList(1, car.path.size).sumBy { street ->
                streets.find { it.name == street }!!.time
            }
            //println("Car ${car.id} travel time = ${car.travelTime}")
        }
        // Optimization: completely ignore cars that will never make it in time
        cars = cars.filter { it.travelTime <= D }.toMutableList()
    }

    private fun getStreetsLoad() {
        streets.forEach { street ->
            street.load = cars.count { street.name in it.path }
            street.averageLoad = cars.count { street.name in it.path } / D
        }
        intersections.forEach { intersection ->
            intersection.load = intersection.incoming.sumOf { it.load }
        }
    }

    fun schedule(index: Int) {
        val iteration = listOf(D, D / 20, D / 22, 1, D / 50, D / 50)
        computeTravelTimes()
        getStreetsLoad()
        intersections.forEach { intersection ->
            val incomingStreetsSortedByLoad =
                intersection.incoming.filter { it.load > 0 }.sortedBy { street -> street.load }
            val trafficLightSchedules = mutableListOf<TrafficLightSchedule>()

            incomingStreetsSortedByLoad.forEach { street ->
                val trafficLightSchedule =
                    TrafficLightSchedule(
                        street.name,
                        (iteration[index] / incomingStreetsSortedByLoad.size / intersection.load * street.load).coerceAtLeast(1).coerceAtMost(D)
                    )
                if (trafficLightSchedule.time != 0)
                    trafficLightSchedules.add(trafficLightSchedule)
                //println("Street ${street.name} load: ${street.load} seconds: ${(cycle/ intersection.load * street.load)}")
            }
            val intersectionSchedule =
                IntersectionSchedule(intersection.id, incomingStreetsSortedByLoad.size, trafficLightSchedules)
            if (intersectionSchedule.trafficLightSchedules.isNotEmpty() && intersectionSchedule.notEverythingIsRed())
                schedule.add(intersectionSchedule)
        }
    }

    private fun IntersectionSchedule.notEverythingIsRed(): Boolean {
        return this.trafficLightSchedules.sumOf { it.time } != 0
    }

    fun output() {
        val file = File(outputFile)
        file.printWriter().use { out ->
            out.println(schedule.size)
            schedule.forEach { intersectionSchedule ->
                out.println(intersectionSchedule.id.toString())
                out.println(intersectionSchedule.incoming.toString())

                intersectionSchedule.trafficLightSchedules.forEach {
                    out.print(it.street + " ")
                    out.println(it.time.toString())
                }
            }
        }
        println("Schedule for dataset $outputFile done")
    }
}

fun main() {
    val datasets = listOf("a", "b", "c", "d", "e", "f")
    datasets.forEach { ds ->
        val input = "data/$ds.txt"
        val output = "data/out_$ds.txt"

        // Schedule traffic lights for the city and output the schedule
        CityPlan(input, output).let {
            it.schedule(datasets.indexOf(ds))
            it.output()
        }
    }
}