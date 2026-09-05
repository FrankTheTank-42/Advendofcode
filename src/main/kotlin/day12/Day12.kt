package de.winkler.day12

import sun.management.MemoryNotifInfoCompositeData.getCount
import java.io.File
import java.util.Objects
import kotlin.collections.contentDeepHashCode

var example = File("./src/main/kotlin/day12/example.txt").readLines()
var input = File("./src/main/kotlin/day12/input.txt").readLines()


fun main() {

    var lines = example

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
        var d = Solver(presents, region)
        if (d.rec()) total++
         println("trycount: ${d.trycount}")
        println("OpenPixel ${d.openpixel}")
        println("Open9 ${d.open9}")
    }
    println(total)


}


class Solver(
    var presents: List<Present>,
    var toBePlaced: IntArray,
    private val area: Array<BooleanArray>,
    private val states: MutableSet<Int>,
    var openpixel: Int,
    var open9: Int,
    var trycount:Int = 0,
    var ocount: Int = 0
) {
    constructor(presents: List<Present>, region: Region) : this(
        presents,
        region.presents.toIntArray(),
        Array(region.y) { BooleanArray(region.x) },
        mutableSetOf(),
        (region.x * region.y) - region.presents.indices.sumOf { region.presents[it] * presents[it].count },
        ((region.x / 3 * 3) * (region.y / 3 * 3)) - region.presents.toIntArray().sumOf { it * 9 },
        0,0
    )

    fun hash(area: Array<BooleanArray>, toBePlaced: IntArray): Int {
        return Objects.hash(area.contentDeepHashCode(), toBePlaced.contentHashCode())
    }

    fun rec(): Boolean {
        trycount++
        if (trycount % 1000000 == 0) {
            //println("trycount: $trycount ")
        }
        if (toBePlaced.sum() == 0) {
            return true
        }

        var hash = hash(area, toBePlaced)

        if (states.contains(hash)) {
            //  return false
        }

        states.add(hash)


        var f = toBePlaced.indexOfFirst { it != 0 }
        toBePlaced[f]--
        var p = presents[f]
        for (variant in p.variants) {
            for (y in 0..area.size - 3) {
                for (x in 0..area[0].size - 3) {
                    if (area.tryAdd(variant, x, y)) {
                        if (rec()) {
                            return true
                        } else {
                            area.subtract(variant, x, y)
                        }
                    }
                }
            }
        }
        toBePlaced[f]++
        return false
    }

}

class Present(shape: List<List<Boolean>>) {
    val area: Array<BooleanArray> = getArea(shape)
    val variants: List<Array<BooleanArray>> = createVariants(area)
    val count = area.sumOf { a -> a.sumOf { b -> if (b) 1 else 0 } }

    companion object {
        private fun createVariants(area: Array<BooleanArray>): List<Array<BooleanArray>> {
            var set = mutableSetOf<Array<BooleanArray>>()
            var a = area
            for (o in 0..3) {
                set.add(a)
                a = a.turned()
            }
            a = area.flipped()
            for (o in 0..3) {
                set.add(a)
                a = a.turned()
            }



            return set.distinctBy(::arrayToString).toList()
        }
    }
}

fun arrayToString(a: Array<BooleanArray>) = a.joinToString("\n") { a -> a.joinToString("") { if (it) "#" else "." } }


data class Region(var x: Int, var y: Int, var presents: List<Int>)

fun getArea(shape: List<List<Boolean>>): Array<BooleanArray> {
    val result = Array(
        shape.size
    ) { BooleanArray(shape[0].size) }
    for (y in shape.indices) {
        for (x in shape[y].indices) {
            result[y][x] = shape[y][x]
        }
    }
    return result
}

fun Array<BooleanArray>.tryAdd(part: Array<BooleanArray>, xstart: Int, ystart: Int): Boolean {
    if (xstart + part[0].size > this[0].size || ystart + part.size > size) {
        println("this:")
        println(toString())
        println("$xstart, $ystart")
        throw RuntimeException()
    }

    for (y in 0 until part.size) {
        for (x in 0 until part[0].size) {
            if (this[y + ystart][x + xstart] && part[y][x]) {
                return false
            }
        }
    }

    for (y in 0 until part.size) {
        for (x in 0 until part[0].size) {
            this[y + ystart][x + xstart] = this[y + ystart][x + xstart] || part[y][x]
        }
    }
    return true
}

fun Array<BooleanArray>.subtract(part: Array<BooleanArray>, xstart: Int, ystart: Int) {
    for ((y, element) in part.withIndex()) {
        for (x in part[0].indices) {
            this[y + ystart][x + xstart] = this[y + ystart][x + xstart] && !element[x]
        }
    }
}

fun Array<BooleanArray>.flipped(): Array<BooleanArray> {
    val result = Array(3) { BooleanArray(3) { false } }
    for (y in result.indices) {
        for (x in result[0].indices) {
            result[y][x] = this[x][2 - y]
        }
    }
    return result
}

fun Array<BooleanArray>.turned(): Array<BooleanArray> {
    val result = Array(3) { BooleanArray(3) { false } }
    for (y in 0 until 3) {
        for (x in 0 until 3) {
            result[y][x] = this[x][2 - y]
        }
    }
    return result
}
