package com.example.prm1

interface Navigable {
    enum class Destination {
        List, Add, Edit, Display
    }
    fun navigate(to: Destination, id: Long? = null)


}