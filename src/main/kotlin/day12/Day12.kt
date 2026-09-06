package de.winkler.day12

import sun.management.MemoryNotifInfoCompositeData.getCount
import java.io.File
import java.util.Objects
import kotlin.collections.contentDeepHashCode

var example = File("./src/main/kotlin/day12/example.txt").readLines()
var input = File("./src/main/kotlin/day12/input.txt").readLines()


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
    var count1 = 0
    var mino = 1000
    var maxo = -10

    for (region in regions) {
        var d = Solver(presents, region)
        var start = d.openpixel-region.presents.indices.sumOf { region.presents[it] * (9-presents[it].count) }

        var s =d.rec()
        if (s) total++
        if(!s && d.trycount != 1 )
        println("solveable: $s trycount: ${d.trycount} ")
        if (d.trycount != 1) count1++
        if(start < mino && s) mino = start
        if(start > maxo && !s) maxo = start
        println("solveable: $s start $start end ${d.openpixel}  diff " + ( start-d.openpixel).toString())
    }

    println("1s: $count1 mino: $mino maxo: $maxo")
    println(total)


}


class Solver(
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
                    var r = tryAdd(variant, x, y)
                    if (r != Int.MIN_VALUE) {
                        openpixel -= r
                        if (rec()) {
                            return true
                        }
                        openpixel += subtract(variant, x, y)

                    }
                }
            }
        }
        toBePlaced[f]++
        return false
    }

    fun tryAdd(part: Array<BooleanArray>, xstart: Int, ystart: Int): Int {

        for ((y, element) in part.withIndex()) {
            for (x in part[0].indices) {
                if (area[y + ystart][x + xstart] < 0 && element[x]) {
                    return Int.MIN_VALUE
                }
            }
        }
        var result = 0
        for ((y, element) in part.withIndex()) {
            for (x in part[0].indices) {
                if (element[x]) {
                    result -= area[y + ystart][x + xstart]
                    area[y + ystart][x + xstart] = -area[y + ystart][x + xstart] - 1
                } else if (area[y + ystart][x + xstart] >= 0) {
                    result++
                    area[y + ystart][x + xstart]++
                }
            }
        }
        return result
    }


    fun subtract(part: Array<BooleanArray>, xstart: Int, ystart: Int): Int {
        var result = 0
        for ((y, element) in part.withIndex()) {
            for (x in part[0].indices) {
                if (element[x]) {
                    area[y + ystart][x + xstart] = -area[y + ystart][x + xstart] - 1
                    result -= area[y + ystart][x + xstart]
                } else if (area[y + ystart][x + xstart] >= 0) {
                    area[y + ystart][x + xstart]--
                    result++
                }
            }
        }

        return result
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
