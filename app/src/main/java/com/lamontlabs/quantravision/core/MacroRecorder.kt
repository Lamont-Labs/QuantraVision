package com.lamontlabs.quantravision.core

import android.content.Context
import android.os.SystemClock
import android.view.KeyEvent
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

/**
 * MacroRecorder
 * - Lightweight, deterministic macro system for power users.
 * - Primary use: one-tap Multi-Time-Frame (MTF) snapshot workflow.
 * - Works entirely offline; stores macros in SharedPreferences.
 *
 * Concepts
 *  - Macro = a named list of Actions executed in order.
 *  - Trigger = a simple hotkey (KEYCODE_*), overlay button ID, or gesture tag.
 *  - Actions provided here are abstract; wire your snapshot/capture callbacks.
 */
class MacroRecorder(private val ctx: Context) {

    // ---- Public API ---------------------------------------------------------

    fun registerActionFactory(type: String, factory: (Map<String, String>) -> MacroAction) {
        actionFactories[type] = factory
    }

    fun bind(trigger: Trigger, macro: Macro) {
        bindings[trigger.key()] = macro
        persist()
    }

    fun unbind(trigger: Trigger) {
        bindings.remove(trigger.key())
        persist()
    }

    fun triggers(): List<Trigger> = bindings.keys.map { Trigger.parse(it) }

    fun onKeyEvent(ev: KeyEvent): Boolean {
        if (ev.action != KeyEvent.ACTION_DOWN) return false
        val key = Trigger.Key(ev.keyCode).key()
        val macro = bindings[key] ?: return false
        execute(macro)
        return true
    }

    fun invoke(trigger: Trigger): Boolean {
        val macro = bindings[trigger.key()] ?: return false
        execute(macro)
        return true
    }

    fun load() {
        if (loaded) return
        val raw = prefs().getString(PREF_BINDS, "") ?: ""
        if (raw.isBlank()) { loaded = true; return }
        raw.split("|").forEach { entry ->
            if (entry.isBlank()) return@forEach
            val parts = entry.split("=>")
            if (parts.size != 2) return@forEach
            val trig = parts[0]
            val macro = Macro.deserialize(parts[2 - 1])
            bindings[trig] = macro
        }
        loaded = true
    }

    // ---- Execution ----------------------------------------------------------

    private fun execute(m: Macro) {
        val now = SystemClock.elapsedRealtime()
        // Simple debounce: ignore re-trigger within 300 ms
        if (now - lastExecTs < 300) return
        lastExecTs = now

        m.actions.forEach { act ->
            try { act.execute(ctx) } catch (_: Throwable) { /* ignore per action */ }
        }
    }

    // ---- Persistence --------------------------------------------------------

    private fun persist() {
        val sb = StringBuilder()
        bindings.forEach { (k, v) ->
            sb.append(k).append("=>").append(v.serialize()).append("|")
        }
        prefs().edit().putString(PREF_BINDS, sb.toString()).apply()
    }

    private fun prefs() = ctx.getSharedPreferences("qv_macros", Context.MODE_PRIVATE)

    // ---- Internals ----------------------------------------------------------

    private val bindings = ConcurrentHashMap<String, Macro>()
    private val actionFactories = ConcurrentHashMap<String, (Map<String, String>) -> MacroAction>()
    private var loaded = false
    private var lastExecTs = 0L

    companion object {
        private const val PREF_BINDS = "bindings"
    }
}

/** Trigger types supported by the recorder. */
sealed class Trigger {
    data class Key(val keyCode: Int) : Trigger()
    data class OverlayButton(val id: String) : Trigger()
    data class Gesture(val tag: String) : Trigger()

    fun key(): String = when (this) {
        is Key -> "key:$keyCode"
        is OverlayButton -> "btn:$id"
        is Gesture -> "ges:$tag"
    }

    companion object {
        fun parse(s: String): Trigger = when {
            s.startsWith("key:") -> Key(s.removePrefix("key:").toInt())
            s.startsWith("btn:") -> OverlayButton(s.removePrefix("btn:"))
            s.startsWith("ges:") -> Gesture(s.removePrefix("ges:"))
            else -> Gesture("unknown")
        }
    }
}

/** A macro is a name plus an ordered list of actions. */
data class Macro(val name: String, val actions: List<MacroAction>) {
    fun serialize(): String = buildString {
        append(name).append("#")
        actions.forEachIndexed { i, a ->
            if (i > 0) append(",")
            append(a.type()).append("{").append(a.params().entries.joinToString(";") { "${it.key}=${it.value}" }).append("}")
        }
    }
    companion object {
        fun deserialize(s: String): Macro {
            val i = s.indexOf('#')
            val name = if (i >= 0) s.substring(0, i) else "macro"
            val rest = if (i >= 0) s.substring(i + 1) else ""
            val acts = if (rest.isBlank()) emptyList() else rest.split(",").mapNotNull { MacroAction.parse(it) }
            return Macro(name, acts)
        }
    }
}

/** Base interface for actions; concrete actions below are deterministic and offline. */
interface MacroAction {
    fun execute(context: Context)
    fun type(): String
    fun params(): Map<String, String>

    companion object {
        fun parse(raw: String): MacroAction? {
            val tStart = raw.indexOf('{')
            val tEnd = raw.lastIndexOf('}')
            if (tStart <= 0 || tEnd <= tStart) return null
            val type = raw.substring(0, tStart)
            val body = raw.substring(tStart + 1, tEnd)
            val map = if (body.isBlank()) emptyMap() else body.split(";").associate {
                val kv = it.split("="); kv[0] to (kv.getOrNull(1) ?: "")
            }
            return when (type) {
                MTFSnapshot.TYPE -> MTFSnapshot.from(map)
                Delay.TYPE -> Delay.from(map)
                ToastHint.TYPE -> ToastHint.from(map)
                else -> null
            }
        }
    }
}

/** Action: capture MTF snapshots (user-configured labels like "5m,15m,1h"). */
class MTFSnapshot private constructor(private val timeframes: List<String>) : MacroAction {
    override fun execute(context: Context) {
        // Hook this into your existing screenshot + cache flow.
        // Deterministic stub: write labels into an in-memory cache for the confluence engine to consume.
        MTFBus.push(timeframes)
    }
    override fun type() = TYPE
    override fun params() = mapOf("tfs" to timeframes.joinToString(","))
    companion object {
        const val TYPE = "mtf"
        fun from(map: Map<String, String>) = MTFSnapshot(map["tfs"]?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList())
        fun of(vararg tfs: String) = MTFSnapshot(tfs.toList())
    }
}

/** Action: deterministic delay to allow UI settle before next action. */
class Delay private constructor(private val millis: Long) : MacroAction {
    override fun execute(context: Context) { try { Thread.sleep(millis.coerceIn(0, 2000)) } catch (_: InterruptedException) {} }
    override fun type() = TYPE
    override fun params() = mapOf("ms" to millis.toString())
    companion object {
        const val TYPE = "delay"
        fun from(map: Map<String, String>) = Delay(map["ms"]?.toLongOrNull() ?: 150L)
        fun of(ms: Long) = Delay(ms)
    }
}

/** Action: show unobtrusive on-screen hint via Android Toast. */
class ToastHint private constructor(private val message: String) : MacroAction {
    override fun execute(context: Context) {
        android.widget.Toast.makeText(context.applicationContext, message, android.widget.Toast.LENGTH_SHORT).show()
    }
    override fun type() = TYPE
    override fun params() = mapOf("msg" to message)
    companion object {
        const val TYPE = "toast"
        fun from(map: Map<String, String>) = ToastHint(map["msg"] ?: "")
        fun of(msg: String) = ToastHint(msg)
    }
}

/**
 * Simple in-memory bus to pass requested MTF labels to your confluence engine.
 * Replace with your existing event system if present.
 */
object MTFBus {
    private val listeners = CopyOnWriteArrayList<(List<String>) -> Unit>()
    fun on(handler: (List<String>) -> Unit) { listeners.add(handler) }
    fun off(handler: (List<String>) -> Unit) { listeners.remove(handler) }
    fun push(tfs: List<String>) { listeners.forEach { h -> try { h(tfs) } catch (_: Throwable) {} } }
}
