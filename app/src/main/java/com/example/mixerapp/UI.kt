package com.example.mixerapp
import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import kotlin.math.ln
import kotlin.math.log10


@SuppressLint("SuspiciousIndentation")
@Composable
fun MixerScreen(navController: NavController, oscManager: OscManager) {
    val coroutineScope = rememberCoroutineScope()

        Surface (
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF353535)
        )
        {
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                var volume by remember { mutableStateOf(0.5f) }
                CustomFader(
                    channel = 0,
                    volume = volume,
                    onVolumeChange = { newVol ->
                        volume = newVol
                        coroutineScope.launch {
                            oscManager.sendMasterVolume(newVol)
                        }
                    },
                    meterValueR = oscManager.masterListenerR(),
                    meterValueL = oscManager.masterListenerL(),
                    oscManager = oscManager
                )

                Box(
                    modifier = Modifier
                        .width(1.dp).height(340.dp).background(Color.Black)
                        .align(Alignment.CenterVertically)
                )

                repeat(9) { channel ->
                    var volume by remember { mutableStateOf(0.5f) }

                    CustomFader(
                        channel = channel + 1,
                        volume = volume,
                        onVolumeChange = { newVol ->
                            volume = newVol
                            coroutineScope.launch {
                                oscManager.sendVolumeFader(channel + 1, newVol)
                            }
                        },
                        meterValueR = oscManager.getMeterValueR(channel + 1),
                        meterValueL = oscManager.getMeterValueL(channel + 1),
                        oscManager = oscManager
                    )

                    Box(
                        modifier = Modifier
                            .width(1.dp).height(340.dp).background(Color.Black)
                            .align(Alignment.CenterVertically)
                    )

                }
                Box(
                    modifier = Modifier.requiredSize(20.dp).align(Alignment.Bottom).offset(x = -15.dp, y = -5.dp)
                        .clickable { navController.navigate("settings") }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.settings),
                        contentDescription = "Clickable Image"
                    )
                }
            }
        }
}



@Composable
fun CustomFader(
    channel: Int,
    volume: Float,
    onVolumeChange: (Float) -> Unit,
    meterValueR: Float,
    meterValueL: Float,
    oscManager: OscManager
) {
    // Dimensioni principali
    val trackWidth = 75.dp    // larghezza del fader
    val trackHeight = 370.dp  // altezza del fader
    val knobSize = 35.dp      // dimensione del knob
    val coroutineScope = rememberCoroutineScope()


    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        //Spacer(modifier = Modifier.height(8.dp))
        var color = 0xFF202020
        if(channel == 0) color = 0xFF2A2A2A else color = 0xFF383838

        //Tutto il canale
        Column(
            modifier = Modifier
            .width(trackWidth).height(trackHeight)
            .padding(0.dp)
            .background(Color(color), shape = RoundedCornerShape(0.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        )
        {

            // Text channel, Panning, Mute, Solo
            Column(
                modifier = Modifier
                    .padding(start = 5.dp, end = 5.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Testo channel
               // var text by remember { mutableStateOf("CH $channel") }
                var text by remember { mutableStateOf(if (channel == 0) "MASTER" else "CH $channel") }
                val isFocused = remember { mutableStateOf(false) }

                BasicTextField(
                    value = text,
                    textStyle = TextStyle(fontSize = 11.sp,color = Color.White, textAlign = TextAlign.Center),
                    onValueChange = { newText ->
                        text = newText },
                    singleLine = true,
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.height(30.dp).width(60.dp).padding(0.dp)
                        .onFocusChanged { focusState -> isFocused.value = focusState.isFocused }
                        .background(
                            if (isFocused.value) Color(0xFF444444) else Color.Transparent, // sfondo dinamico
                            shape = RoundedCornerShape(4.dp)
                        ),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            //.padding(horizontal = 4.dp), // opzionale, un po' di spazio laterale
                            contentAlignment = Alignment.Center
                        ) {
                            innerTextField()
                        }
                    }
                )

                // Panning
                var panValue by remember { mutableStateOf(0f) }  // valore di pan da -1 a 1

                PanControlSlider(
                    panValue = panValue,
                    onPanChange = { newPan ->
                        panValue = newPan
                        coroutineScope.launch {
                            oscManager.sendPan(channel, newPan)
                        }
                    }
                )


                val interactionSource2 = remember { MutableInteractionSource() }
                val isPressed2 by interactionSource2.collectIsPressedAsState()
                val buttonColor2 = if (isPressed2) {
                    Color(0xFF1A3242) // Colore quando premuto
                } else {
                    Color(0xFF4482AC) // Colore normale
                }

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val buttonColor = if (isPressed) {
                    Color(0xFF254547) // Colore quando premuto
                } else {
                    Color(0xFF5CA9AD) // Colore normale
                }

                Button(
                    onClick = { coroutineScope.launch { oscManager.toggleMute(channel) } },
                    interactionSource = interactionSource,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor
                    ),
                    modifier = Modifier.height(25.dp).width(50.dp).offset(y = -18.dp),
                    shape = RoundedCornerShape(4.dp),
                    //colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A8A8A)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Mute", color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(5.dp))
                Button(
                    onClick = { coroutineScope.launch { oscManager.toggleSolo(channel) } },
                    interactionSource = interactionSource2,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = buttonColor2
                    ),
                    modifier = Modifier.height(25.dp).width(50.dp).offset(y = -18.dp),
                    shape = RoundedCornerShape(4.dp),
                    //colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A8A8A)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("Solo", color = Color.White, fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.height(5.dp))
                val mediaMeterValue = (meterValueL+meterValueR)/2
                val dbText = if (mediaMeterValue == 0f) "-∞ dB" else "${String.format("%.1f", 20 * log10(mediaMeterValue))} dB"
                Text(text = dbText, fontSize = 15.sp, color = Color.White)
            }

            // Box contenitore fader
            Box(
                modifier = Modifier
                    .width(trackWidth).height(trackHeight)
                    //.padding(top = 10.dp, bottom = 10.dp)
                    .align(Alignment.CenterHorizontally)
                    .background(Color(color), shape = RoundedCornerShape(0.dp))
            ){
                fun logMeterLevel(value: Float): Float {
                    return if (value <= 0f) 0f else (ln(1 + 9 * value) / ln(10f))
                }
                // Box meterlevelR
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd).padding(top = 15.dp, bottom = 10.dp)
                        .width(3.dp).height(trackHeight /2 * logMeterLevel(meterValueR)-25.dp)
                        .offset(x = -11.dp)
                        .background(brush = if (meterValueR >= 0.95f) {
                            // Se siamo in clipping, tutto rosso
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF5252), Color(0xFFFF5252)))
                        } else {
                            // Gradiente normale
                            Brush.verticalGradient(colors = listOf(Color(0xFFFFFF3F), Color(0xFF007F5F)))
                        }
                            ,shape = RoundedCornerShape(4.dp)  )
                )

                // Box meterlevelL
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd).padding(top = 15.dp, bottom = 10.dp)
                        .width(3.dp).height(trackHeight / 2 * logMeterLevel(meterValueL)-25.dp)
                        .offset(x = -17.dp)
                        .background(brush = if (meterValueL >= 0.95f) {
                            // Se siamo in clipping, tutto rosso
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFF5252), Color(0xFFFF5252)))
                        } else {
                            // Gradiente normale
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFFFFFF3F), Color(0xFF007F5F)))
                        }
                            ,shape = RoundedCornerShape(4.dp)  )
                )

                var boxHeightPx by remember { mutableStateOf(0f) }

                //Track
                Box(
                    modifier = Modifier
                        .height(trackHeight).width(10.dp).padding(top = 10.dp, bottom = 10.dp)
                        .offset(x = 25.dp)
                        .onGloballyPositioned {
                            boxHeightPx = it.size.height.toFloat()
                        }
                ) {
                    val knobSizePx = with(LocalDensity.current) { knobSize.toPx() }
                    val knobOffsetPx = (boxHeightPx - knobSizePx) * (1f - volume)
                    val knobOffsetDp = with(LocalDensity.current) { knobOffsetPx.toDp() }

                    // Track background
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF131313), shape = RoundedCornerShape(4.dp))
                            .align(Alignment.Center)
                    )

                    // Riempimento (dal bottom fino al centro del knob)
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(with(LocalDensity.current) { (boxHeightPx * volume).toDp() })
                            .align(Alignment.BottomCenter)
                            .background(Color(0xFFAAD9EC), shape = RoundedCornerShape(4.dp))
                    )

                    // Knob posizionato in base al volume
                    Image(
                        painter = painterResource(id = R.drawable.knob_colored_fader),
                        contentDescription = "Knob Fader",
                        modifier = Modifier
                            .size(knobSize)
                            .requiredSize(knobSize)
                            .offset(y = knobOffsetDp)
                            .align(Alignment.TopCenter)
                    )

                    // Slider invisibile per l'interazione
                    Slider(
                        value = volume,
                        onValueChange = onVolumeChange,
                        valueRange = 0f..1f,
                        modifier = Modifier
                            .requiredSize(width = 150.dp, height = trackWidth).offset(y = 30.dp)
                            .rotate(-90f),
                        colors = SliderDefaults.colors(
                            thumbColor = Color.Transparent,
                            activeTrackColor = Color.Transparent,
                            inactiveTrackColor = Color.Transparent
                        )
                    )
                }



            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanControlSlider(
    panValue: Float,
    onPanChange: (Float) -> Unit
) {

    /*
    Text(
        text = "Panning", color = Color.White, style = MaterialTheme.typography.labelSmall
    )*/
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(5.dp).offset(y = -15.dp)
    ) {
        Text(
            text = "L", color = Color.White, style = MaterialTheme.typography.labelSmall
        )
        Slider(
            value = panValue,
            onValueChange = { newValue -> onPanChange(newValue) },
            valueRange = -1f..1f,
            modifier = Modifier
                .fillMaxWidth().weight(1f).padding(0.dp),
            track = {
                // Traccia sottile
                Box(
                    modifier = Modifier
                        .fillMaxWidth().height(4.dp).background(Color(0xFF1B1E1F), RoundedCornerShape(1.5.dp))
                )
            },
            thumb = {
                // Thumb personalizzato più piccolo
                Box(
                    modifier = Modifier.width(5.dp).height(25.dp)
                        .background(Color(0xFF25A8FF), shape = CircleShape)
                )
            },
            colors = SliderDefaults.colors(
                thumbColor = Color.Cyan,
                activeTrackColor = Color(0xFF80DEEA),
                inactiveTrackColor = Color(0xFF37474F)
            )
        )

        Text(
            text = "R", color = Color.White, style = MaterialTheme.typography.labelSmall
        )
    }

}

@Composable
fun SettingsScreen(navController: NavController, oscManager: OscManager) {
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize().padding(15.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Impostazioni", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(10.dp))
        // Campo di testo per cambiare IP
        var ipAddress by remember { mutableStateOf("192.168.1.18") }
        var port by remember { mutableStateOf("8000") }

        OutlinedTextField(
            value = ipAddress,
            onValueChange = { ipAddress = it },
            label = { Text("IP Mixer") },
            modifier = Modifier.width(200.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedTextField(
            value = port,
            onValueChange = { port = it },
            label = { Text("Porta Mixer") },
            modifier = Modifier.width(200.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                coroutineScope.launch { oscManager.connect(ipAddress, port.toInt()) }// Connetti con nuovo IP/porta
            },
            modifier = Modifier.width(200.dp)
        ) {
            Text("Salva Impostazioni")
        }

        Spacer(modifier = Modifier.height(5.dp))

        Button(
            onClick = { navController.popBackStack() }, // Torna al Mixer
            modifier =Modifier.width(200.dp)
        ) {
            Text("Torna al Mixer")
        }
    }
}


/*
fun panToAngle(pan: Float): Float {
    // Limita il valore tra -1 e 1, e mappa su -135° a 135°
    return (pan.coerceIn(-1f, 1f)) * 135f
}

fun angleToPan(angle: Float): Float {
    // Limita l'angolo tra -135° e 135° e mappa su -1 a 1
    return (angle / 135f).coerceIn(-1f, 1f)
}


@Composable
fun KnobPanControl(
    modifier: Modifier = Modifier.size(50.dp),
    initialValue: Float = 0f,
    onPanChange: (Float) -> Unit
) {
    // Conversioni iniziali
    var angle by remember { mutableStateOf(panToAngle(initialValue)) }
    var panValue by remember { mutableStateOf(initialValue) }

    val density = LocalDensity.current
    val strokeWidth = 8f

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    val size = this@pointerInput.size
                    val center = Offset(size.width / 2f, size.height / 2f)

                    val x = change.position.x - center.x
                    val y = change.position.y - center.y

                    var theta = atan2(y, x) * (180f / PI).toFloat()

                    // Normalizza angolo per il range [-135°, 135°]
                    if (theta < -135f) theta = -135f
                    if (theta > 135f) theta = 135f

                    angle = theta
                    panValue = angleToPan(theta)
                    onPanChange(panValue)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - strokeWidth * 2

            // Cerchio esterno
            drawCircle(
                color = Color.DarkGray,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Indicatore (linea)
            val angleRad = Math.toRadians((angle - 90).toDouble())
            val lineLength = radius * 0.8f

            val end = Offset(
                x = center.x + cos(angleRad).toFloat() * lineLength,
                y = center.y + sin(angleRad).toFloat() * lineLength
            )

            drawLine(
                color = Color.Cyan,
                start = center,
                end = end,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
        }
        val panText = when {
            panValue < 0f -> "L ${(panValue * -100).toInt()}%"
            panValue > 0f -> "R ${(panValue * 100).toInt()}%"
            else -> "C"
        }
        // Testo del valore
        Text(
            //text = "PAN: ${"%.2f".format(panValue)}",
            text = panText,
            color = Color.White,
            fontSize = 14.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 4.dp)
        )
    }
}
*/







