package com.bridgeai.android.ui

object Selection {
    enum class Agent { Claude, Codex }
    enum class Mode  { Free, Classic }
    var agent: Agent? = null
    var mode:  Mode?  = null
}
