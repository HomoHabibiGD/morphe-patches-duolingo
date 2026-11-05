package app.template.patches.duolingo

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
import app.morphe.patcher.fingerprint
import app.morphe.patcher.methodCall
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.AccessFlags
import com.android.tools.smali.dexlib2.Opcode

private val linearProgressBarFingerprint = fingerprint {
    accessFlags(AccessFlags.PUBLIC, AccessFlags.STATIC)
    returns("V")
    parameters("Ltlv;", "F", "F", "Lkotlin/jvm/functions/Function0;", "I")
    instructions(
        methodCall(
            "Ltlv;",
            "e",
            listOf(
                "Ltlv;",
                "F",
                "F",
                "Lkotlin/jvm/functions/Function0;",
                "Landroid/view/animation/LinearInterpolator;",
                "I",
            ),
            "Landroid/animation/ValueAnimator;",
            Opcode.INVOKE_STATIC_RANGE,
        ),
    )
    custom { method, classDef -> method.name == "b" && classDef.type == "Ltlv;" }
}

val duolingoLinearProgressBarPatch = bytecodePatch(
    name = "Duolingo Linear Progress Bar",
    description = "Replaces Duolingo's OvershootInterpolator with a LinearInterpolator for smoother progress bars.",
) {
    execute {
        val match = linearProgressBarFingerprint.match()
        val setupIndex = match.instructionMatches.single().index - 5
        val method = match.method

        method.removeInstructions(setupIndex, 2)
        method.addInstructions(
            setupIndex,
            """
                new-instance v5, Landroid/view/animation/LinearInterpolator;
                invoke-direct {v5}, Landroid/view/animation/LinearInterpolator;-><init>()V
                const/4 v6, 0x0
            """.trimIndent(),
        )
    }
}