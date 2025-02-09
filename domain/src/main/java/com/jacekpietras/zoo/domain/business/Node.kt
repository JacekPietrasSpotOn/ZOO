package com.jacekpietras.zoo.domain.business

import com.jacekpietras.core.PointD
import com.jacekpietras.core.haversine

internal class Node(
    val point: PointD,
    val edges: MutableSet<Edge> = mutableSetOf()
) {

    val x: Double
        get() = point.x

    val y: Double
        get() = point.y

    fun connect(node: Node, technical: Boolean, backward:Boolean) {
        edges.add(Edge(node, technical, backward))
    }

    fun connectAndCalc(node: Node, technical: Boolean, backward:Boolean) {
        edges.add(Edge(node, technical, backward, haversine(this.x, this.y, node.x, node.y)))
    }

    fun disconnect(node: Node) {
        edges.remove(edges.first { it.node == node })
    }

    override fun toString() = "Node($x, $y),"
}