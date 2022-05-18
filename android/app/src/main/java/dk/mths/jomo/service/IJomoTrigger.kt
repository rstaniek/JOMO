package dk.mths.jomo.service

interface IJomoTrigger {
    fun enable()
    fun disable()
    fun isEnabled(): Boolean
}