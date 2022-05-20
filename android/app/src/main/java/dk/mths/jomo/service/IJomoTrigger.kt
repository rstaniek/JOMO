package dk.mths.jomo.service

interface IJomoTrigger {
    fun enable(packageName: String)
    fun disable()
    fun isEnabled(): Boolean
}