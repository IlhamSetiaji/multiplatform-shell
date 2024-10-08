package org.prasi.shell.utils

fun Pair<Number, Number>?.isZero(): Boolean {
    return this == null || (first == 0 && second == 0)
}

fun Pair<Number, Number>?.notZero(): Boolean {
    return !isZero()
}
