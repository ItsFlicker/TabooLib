package taboolib.common.platform

import taboolib.common.LifeCycle

@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
annotation class Awake(val value: LifeCycle = LifeCycle.CONST)