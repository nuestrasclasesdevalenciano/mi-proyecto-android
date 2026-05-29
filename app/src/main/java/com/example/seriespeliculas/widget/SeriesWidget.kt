package com.example.seriespeliculas.widget

import android.content.Context
import androidx.compose.runtime.produceState
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.seriespeliculas.MainActivity
import com.example.seriespeliculas.data.local.AppDatabase
import com.example.seriespeliculas.data.local.SerieEntity
import com.example.seriespeliculas.data.model.ListaTipo
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class SeriesWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val sugerenciaState = produceState<SerieEntity?>(initialValue = null) {
                val db = AppDatabase.getInstance(context)
                val allSeries = db.serieDao().observeAll().first()
                val porVer = allSeries.filter { it.lista == ListaTipo.POR_VER.name || it.lista == ListaTipo.VIENDO.name }
                value = if (porVer.isNotEmpty()) {
                    porVer[Random.nextInt(porVer.size)]
                } else {
                    null
                }
            }

            GlanceTheme {
                WidgetContent(sugerenciaState.value)
            }
        }
    }

    @androidx.compose.runtime.Composable
    private fun WidgetContent(sugerencia: SerieEntity?) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(8.dp)
                .clickable(actionStartActivity<MainActivity>()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "¿Qué vemos hoy?",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.primary
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(GlanceTheme.colors.secondaryContainer)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxWidth().padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (sugerencia != null) {
                        Text(
                            text = sugerencia.titulo,
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = androidx.glance.text.TextAlign.Center,
                                color = GlanceTheme.colors.onSecondaryContainer
                            )
                        )
                        if (sugerencia.genero != null) {
                            Text(
                                text = sugerencia.genero ?: "",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = GlanceTheme.colors.onSecondaryContainer
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "Añade algo a tu lista",
                            style = TextStyle(fontSize = 14.sp, color = GlanceTheme.colors.onSecondaryContainer)
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Refrescar ↻",
                    modifier = GlanceModifier.clickable(actionRunCallback<RefreshAction>()),
                    style = TextStyle(fontSize = 12.sp, color = GlanceTheme.colors.primary)
                )
            }
        }
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        SeriesWidget().updateAll(context)
    }
}

class SeriesWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SeriesWidget()
}
