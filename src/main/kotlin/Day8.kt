package de.winkler

import kotlin.math.sqrt


fun main() {
    var input = """
        162,817,812
        57,618,57
        906,360,560
        592,479,940
        352,342,300
        466,668,158
        542,29,236
        431,825,988
        739,650,466
        52,470,668
        216,146,977
        819,987,18
        117,168,530
        805,96,715
        346,949,466
        970,615,88
        941,993,340
        862,61,35
        984,92,344
        425,690,689
    """.trimIndent()

    var boxes = input.lines().map { line -> Pointof(line) }.toTypedArray()
    var circuits = mutableMapOf<Point, MutableSet<Point>>()
    boxes.forEach { circuits[it] = mutableSetOf(it) }

    var distances = mutableListOf<Triple<Point, Point, Double>>()
    for (i in 0..boxes.lastIndex) {
        for (j in i + 1..boxes.lastIndex) {
            distances.add(Triple(boxes[i], boxes[j], distance(boxes[i], boxes[j])))
        }
    }

    distances.sortBy { it.third }
    for (i in 0..3) {
        var next = distances[i]
        println("prepare $next")
        println("before   ${circuits[next.first]} ${circuits[next.second]}")
        circuits[next.second]!!.addAll(circuits[next.first]!!)
        circuits[next.first] = circuits[next.second]!!
        println("after ${circuits[next.first]} ${circuits[next.second]}")
        println(check(circuits))
    }

    var total = 1;
    for (circuit in circuits.values.toSet()) {
        total *= circuit.size
    }
    println(total)
}

fun check(circuits: MutableMap<Point, MutableSet<Point>>): Boolean {
    for (circuit in circuits) {
        for (box in circuit.value) {
            if (!circuits[box]!!.contains(circuit.key)) {
                println("Problem with ${circuit.key} with circuit ${circuit.value} with  ${circuits[box]}  from ${box}")
            }
        }
    }
    return true
}

class Point(var x: Int, var y: Int, var z: Int, var name: String) {
    override fun toString(): String = "$name"


    companion object {
        var namecount = 0
    }
}

fun Pointof(s: String): Point {
    var split = s.split(",")
    return Point(split[0].toInt(), y = split[1].toInt(), z = split[2].toInt(), "B${Point.namecount++}")
}


fun distance(p1: Point, p2: Point): Double {
    return sqrt(
        ((p1.x - p2.x) * (p1.x - p2.x) +
                (p1.y - p2.y) * (p1.y - p2.y) +
                (p1.z - p2.z) * (p1.z - p2.z)).toDouble()
    )
}

fun count(circuits: MutableMap<Point, MutableSet<Point>>): Int {
    var result = 0
    for (circuit in circuits.values.toSet()) {
        result += circuit.size
    }
    return result
}

class Day8 {
}