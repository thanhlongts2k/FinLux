package com.finlux.app.core.designsystem.component.form

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finlux.app.core.designsystem.theme.LocalFinluxTokens
import com.finlux.app.core.time.FinanceTime
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * FinluxDateTimePickerSheet - Premium Liquid Glass BottomSheet for Date & Time Selection.
 *
 * Features:
 * 1. Single Source of Truth Timezone: Uses FinanceTime.defaultZone ("Asia/Ho_Chi_Minh").
 * 2. Intra-day & Future Clamping: If allowFutureDates == false, future dates are disabled,
 *    and for today, hours/minutes exceeding the current time are disabled & auto-clamped.
 * 3. Quick Chips: "Hôm nay", "Hôm qua", "2 ngày trước" + Quick Time Chips ("Bây giờ", "08:00", "12:00", "19:00").
 * 4. Zero Overflow: Day cells 36dp with 4dp row spacing, compact horizontal time stepper,
 *    wrapped in verticalScroll to prevent screen clipping.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinluxDateTimePickerSheet(
    selectedDateTime: Instant,
    onDateTimeSelected: (Instant) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "Thời gian giao dịch",
    allowFutureDates: Boolean = false,
    zoneId: ZoneId = FinanceTime.defaultZone,
) {
    val tokens = LocalFinluxTokens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val currentZoned = remember(selectedDateTime, zoneId) { selectedDateTime.atZone(zoneId) }
    var selectedLocalDate by remember(selectedDateTime, zoneId) { mutableStateOf(currentZoned.toLocalDate()) }
    var selectedHour by remember(selectedDateTime, zoneId) { mutableIntStateOf(currentZoned.hour) }
    var selectedMinute by remember(selectedDateTime, zoneId) { mutableIntStateOf(currentZoned.minute) }
    var viewingYearMonth by remember(selectedDateTime, zoneId) { mutableStateOf(YearMonth.from(currentZoned)) }

    val today = remember(zoneId) { LocalDate.now(zoneId) }
    val nowTime = remember(zoneId) { LocalTime.now(zoneId) }

    // Clamp function for intra-day validation
    fun clampTimeIfNeeded(date: LocalDate, hour: Int, minute: Int): Pair<Int, Int> {
        if (!allowFutureDates && date == today) {
            val currentNow = LocalTime.now(zoneId)
            if (hour > currentNow.hour || (hour == currentNow.hour && minute > currentNow.minute)) {
                return currentNow.hour to currentNow.minute
            }
        }
        return hour to minute
    }

    // Auto clamp on initial state or date change
    val (clampedHour, clampedMinute) = clampTimeIfNeeded(selectedLocalDate, selectedHour, selectedMinute)
    if (clampedHour != selectedHour || clampedMinute != selectedMinute) {
        selectedHour = clampedHour
        selectedMinute = clampedMinute
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = tokens.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp),
            ) {
                Surface(
                    shape = CircleShape,
                    color = tokens.onSurfaceVariant.copy(alpha = 0.35f),
                    modifier = Modifier.size(width = 38.dp, height = 4.dp),
                ) {}
            }
        },
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = tokens.primary.copy(alpha = 0.14f),
                        modifier = Modifier.size(38.dp),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = tokens.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.onSurface,
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .background(tokens.surfaceSoft, CircleShape),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Đóng",
                        tint = tokens.onSurface,
                        modifier = Modifier.size(16.dp),
                    )
                }
            }

            // Quick Date Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val quickDates = listOf(
                    "Hôm nay" to today,
                    "Hôm qua" to today.minusDays(1),
                    "2 ngày trước" to today.minusDays(2),
                )

                quickDates.forEach { (label, date) ->
                    val isSelected = selectedLocalDate == date
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) tokens.primary.copy(alpha = 0.14f) else tokens.surfaceSoft,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) tokens.primary else tokens.border,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                selectedLocalDate = date
                                viewingYearMonth = YearMonth.from(date)
                                val (h, m) = clampTimeIfNeeded(date, selectedHour, selectedMinute)
                                selectedHour = h
                                selectedMinute = m
                            },
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            ),
                            color = if (isSelected) tokens.primary else tokens.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 8.dp),
                        )
                    }
                }
            }

            // Calendar Card (Liquid Glass)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    // Month Navigation Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            onClick = { viewingYearMonth = viewingYearMonth.minusMonths(1) },
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Tháng trước",
                                tint = tokens.onSurface,
                                modifier = Modifier.size(14.dp),
                            )
                        }

                        val monthTitle = "Tháng ${viewingYearMonth.monthValue}, ${viewingYearMonth.year}"
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                        )

                        val currentMonth = YearMonth.now(zoneId)
                        val canGoNext = allowFutureDates || viewingYearMonth.isBefore(currentMonth)
                        IconButton(
                            onClick = { if (canGoNext) viewingYearMonth = viewingYearMonth.plusMonths(1) },
                            enabled = canGoNext,
                            modifier = Modifier
                                .size(32.dp)
                                .alpha(if (canGoNext) 1f else 0.25f),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = "Tháng sau",
                                tint = tokens.onSurface,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }

                    // Weekday Labels (T2 .. CN)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        val weekdays = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
                        weekdays.forEach { dayLabel ->
                            Box(
                                modifier = Modifier.size(36.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = dayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tokens.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    // Month Days Grid (7 columns, zero overflow: size 36dp, row spacing 4dp)
                    val firstDayOfMonth = viewingYearMonth.atDay(1)
                    val leadingEmptyDays = (firstDayOfMonth.dayOfWeek.value - 1) // Monday = 1 -> 0 empty
                    val daysInMonth = viewingYearMonth.lengthOfMonth()
                    val totalSlots = leadingEmptyDays + daysInMonth
                    val rowsCount = (totalSlots + 6) / 7

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        for (row in 0 until rowsCount) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                for (col in 0 until 7) {
                                    val slotIndex = row * 7 + col
                                    val dayNumber = slotIndex - leadingEmptyDays + 1

                                    if (dayNumber in 1..daysInMonth) {
                                        val date = viewingYearMonth.atDay(dayNumber)
                                        val isFuture = !allowFutureDates && date.isAfter(today)
                                        val isToday = date == today
                                        val isSelected = date == selectedLocalDate

                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .then(
                                                    if (isSelected) {
                                                        Modifier.background(tokens.primary, CircleShape)
                                                    } else if (isToday) {
                                                        Modifier.border(BorderStroke(1.5.dp, tokens.primary), CircleShape)
                                                    } else {
                                                        Modifier
                                                    }
                                                )
                                                .clickable(
                                                    enabled = !isFuture,
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = ripple(bounded = true),
                                                    onClick = {
                                                        selectedLocalDate = date
                                                        val (h, m) = clampTimeIfNeeded(date, selectedHour, selectedMinute)
                                                        selectedHour = h
                                                        selectedMinute = m
                                                    },
                                                ),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = dayNumber.toString(),
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                                ),
                                                color = when {
                                                    isSelected -> Color.White
                                                    isFuture -> tokens.onSurface.copy(alpha = 0.25f)
                                                    isToday -> tokens.primary
                                                    else -> tokens.onSurface
                                                },
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Time Selector Card (Compact Horizontal Layout, Zero Overflow)
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = tokens.surfaceSoft,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = tokens.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Text(
                            text = "GIỜ GIAO DỊCH (24H)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                            ),
                            color = tokens.onSurfaceVariant,
                        )
                    }

                    // Quick Time Chips with intra-day future disabling
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        val isDateToday = selectedLocalDate == today
                        val nowCurrent = LocalTime.now(zoneId)

                        val timeOptions = listOf(
                            "Bây giờ" to (nowCurrent.hour to nowCurrent.minute),
                            "08:00" to (8 to 0),
                            "12:00" to (12 to 0),
                            "19:00" to (19 to 0),
                        )

                        timeOptions.forEach { (label, time) ->
                            val (h, m) = time
                            val isChipFutureForToday = !allowFutureDates && isDateToday &&
                                (h > nowCurrent.hour || (h == nowCurrent.hour && m > nowCurrent.minute))
                            val isChipSelected = selectedHour == h && selectedMinute == m

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChipSelected) tokens.primary.copy(alpha = 0.14f) else tokens.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isChipSelected) tokens.primary else tokens.border,
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .alpha(if (isChipFutureForToday) 0.3f else 1f)
                                    .clickable(
                                        enabled = !isChipFutureForToday,
                                        onClick = {
                                            selectedHour = h
                                            selectedMinute = m
                                        },
                                    ),
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = if (isChipSelected) FontWeight.Bold else FontWeight.Medium,
                                    ),
                                    color = if (isChipSelected) tokens.primary else tokens.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 6.dp),
                                )
                            }
                        }
                    }

                    // Compact Stepper for Hour and Minute
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Hour Stepper
                        CompactNumberStepper(
                            value = selectedHour,
                            maxValue = if (!allowFutureDates && selectedLocalDate == today) LocalTime.now(zoneId).hour else 23,
                            minValue = 0,
                            onValueChange = { newHour ->
                                val (h, m) = clampTimeIfNeeded(selectedLocalDate, newHour, selectedMinute)
                                selectedHour = h
                                selectedMinute = m
                            },
                            label = "Giờ",
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                            ),
                            color = tokens.onSurface,
                            modifier = Modifier.padding(horizontal = 14.dp),
                        )

                        // Minute Stepper
                        val maxMinute = if (!allowFutureDates && selectedLocalDate == today && selectedHour == LocalTime.now(zoneId).hour) {
                            LocalTime.now(zoneId).minute
                        } else 59

                        CompactNumberStepper(
                            value = selectedMinute,
                            maxValue = maxMinute,
                            minValue = 0,
                            onValueChange = { newMinute ->
                                val (h, m) = clampTimeIfNeeded(selectedLocalDate, selectedHour, newMinute)
                                selectedHour = h
                                selectedMinute = m
                            },
                            label = "Phút",
                        )
                    }
                }
            }

            // Confirm Button
            val selectedInstant = remember(selectedLocalDate, selectedHour, selectedMinute, zoneId) {
                selectedLocalDate.atTime(selectedHour, selectedMinute).atZone(zoneId).toInstant()
            }
            val formattedDateLabel = remember(selectedInstant, zoneId) {
                formatSmartDateTime(selectedInstant, zoneId)
            }

            Button(
                onClick = {
                    onDateTimeSelected(selectedInstant)
                    onDismiss()
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = tokens.primary,
                    contentColor = Color.White,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
            ) {
                Text(
                    text = "Áp dụng: $formattedDateLabel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                )
            }
        }
    }
}

/**
 * Compact horizontal number stepper for Hour / Minute selection.
 * Prevents screen overflow compared to full Material 3 dial clocks.
 */
@Composable
private fun CompactNumberStepper(
    value: Int,
    minValue: Int,
    maxValue: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val tokens = LocalFinluxTokens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Decrement Button
            Surface(
                shape = CircleShape,
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(
                        enabled = value > minValue,
                        onClick = { onValueChange((value - 1).coerceAtLeast(minValue)) },
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "–",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (value > minValue) tokens.onSurface else tokens.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                }
            }

            // Value Display Box
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .size(width = 54.dp, height = 40.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "%02d".format(Locale.US, value),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                        color = tokens.primary,
                    )
                }
            }

            // Increment Button
            Surface(
                shape = CircleShape,
                color = tokens.surface,
                border = BorderStroke(1.dp, tokens.border),
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable(
                        enabled = value < maxValue,
                        onClick = { onValueChange((value + 1).coerceAtMost(maxValue)) },
                    ),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (value < maxValue) tokens.onSurface else tokens.onSurfaceVariant.copy(alpha = 0.3f),
                    )
                }
            }
        }

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = tokens.onSurfaceVariant,
        )
    }
}
