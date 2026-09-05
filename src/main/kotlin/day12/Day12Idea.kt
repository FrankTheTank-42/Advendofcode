package de.winkler.day12

import java.util.Objects
import kotlin.collections.windowed

//var example = File("./src/main/kotlin/day12/example.txt").readLines()
//var input = File("./src/main/kotlin/day12/input.txt").readLines()


fun main() {

    var lines = input

    var indexofFirstRegion = lines.indexOfFirst { it.contains('x') }
    var presents = lines.subList(0, indexofFirstRegion).windowed(5, 5)
        .map { strings ->
            Present(
                strings.subList(1, 4).map { line -> line.toCharArray().map { it == '#' } })
        }

    var regions = lines.subList(indexofFirstRegion, lines.size).map { line ->
        line.split(":").zipWithNext().map {
            Region(
                it.first.split("x").first().toInt(),
                it.first.split("x")[1].toInt(),
                it.second.trim().split(" ").map { it.toInt() }
            )
        }.first()
    }
    var total = 0
    for (region in regions) {
        var solver = Solver2(presents, region)
        if (solver.rec()) total++
        println("trycount: ${solver.trycount} OpenPixel ${solver.openpixel}")
    }
    println(total)


}


class Solver2(
    var presents: List<Present>,
    var toBePlaced: IntArray,
    private val area: Array<IntArray>,
    var openpixel: Int,
    var trycount: Int = 0
) {
    constructor(presents: List<Present>, region: Region) : this(
        presents,
        region.presents.toIntArray(),
        Array(region.y) { IntArray(region.x) },
        (region.x * region.y) - region.presents.indices.sumOf { region.presents[it] * presents[it].count },
    )


    fun rec(): Boolean {
        trycount++
        if (trycount < 100) {
            //println("trycount: $trycount  openpixel: $openpixel")
        }
        if (toBePlaced.sum() == 0) {
            return true
        }

        if (openpixel < 2) {
            return false
        }

        var f = toBePlaced.indexOfFirst { it != 0 }
        toBePlaced[f]--
        var p = presents[f]
        for (variant in p.variants) {
            for (y in 0..area.size - 3) {
                for (x in 0..area[0].size - 3) {
                    var r = area.tryAdd(variant, x, y)
                    if (r != Int.MIN_VALUE) {
                        openpixel -= r
                        if (rec()) {
                            return true
                        }
                        openpixel += area.subtract(variant, x, y)

                    }
                }
            }
        }
        toBePlaced[f]++
        return false
    }

}

fun Array<IntArray>.tryAdd(part: Array<BooleanArray>, xstart: Int, ystart: Int): Int {
    if (xstart + part[0].size > this[0].size || ystart + part.size > size) {
        println("this:")
        println(toString())
        println("$xstart, $ystart")
        throw RuntimeException()
    }

    for ((y, element) in part.withIndex()) {
        for (x in part[0].indices) {
            if (this[y + ystart][x + xstart] < 0 && element[x]) {
                return Int.MIN_VALUE
            }
        }
    }
    var result = 0
    for ((y, element) in part.withIndex()) {
        for (x in part[0].indices) {
            if (element[x]) {
                result -= this[y + ystart][x + xstart]
                this[y + ystart][x + xstart] = -this[y + ystart][x + xstart] - 1
            } else if (this[y + ystart][x + xstart] >= 0) {
                result++
                this[y + ystart][x + xstart]++
            }
        }
    }
    return result
}

fun Array<IntArray>.subtract(part: Array<BooleanArray>, xstart: Int, ystart: Int): Int {
    var result = 0
    for ((y, element) in part.withIndex()) {
        for (x in part[0].indices) {
            if (element[x]) {
                this[y + ystart][x + xstart] = -this[y + ystart][x + xstart] - 1
                result -= this[y + ystart][x + xstart]
            } else if (this[y + ystart][x + xstart] >= 0) {
                this[y + ystart][x + xstart]--
                result++
            }
        }
    }

    return result
}