package com.example.greetingcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.greetingcard.ui.theme.GreetingCardTheme
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.runtime.LaunchedEffect
import android.app.Activity
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset


data class SelectableImage(
    val id: Int,
    val resourceId: Int
)

class newMake : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Intent에서 편집 모드 여부 확인
        val isEditMode = intent.getBooleanExtra("EDIT_MODE", false)

        setContent {
            NewMakeScreen(
                isEditMode = isEditMode,
                onBackClick = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
}

private val BACK_BUTTON_AREA_HEIGHT = 100.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMakeScreen(isEditMode: Boolean = false, onBackClick: () -> Unit) {
    val imageOptions = listOf(
        SelectableImage(id = 1, resourceId = R.drawable.back1),
        SelectableImage(id = 2, resourceId = R.drawable.back2),
        SelectableImage(id = 3, resourceId = R.drawable.back3)
    )

    var currentlySelectedImageResId by remember { mutableStateOf<Int?>(imageOptions.firstOrNull()?.resourceId) }
    var appliedImageResId by remember { mutableStateOf<Int?>(null) }
    var isImageApplied by remember { mutableStateOf(isEditMode) } // 편집 모드면 바로 이미지 적용 상태로

    // 편집 모드일 때 저장된 데이터 로드
    val context = LocalContext.current
    var titleText by remember { mutableStateOf("") }
    var memoText by remember { mutableStateOf("") }

    // 편집 모드일 때 저장된 데이터 로드
    var currentMemoId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            val sharedPreferences = context.getSharedPreferences("MyMemoAppPrefs", Context.MODE_PRIVATE)

            // 가장 최근 메모의 ID 찾기
            val memoIds = sharedPreferences.getStringSet("memo_ids", emptySet()) ?: emptySet()
            val latestMemoId = memoIds.maxByOrNull { it.toLongOrNull() ?: 0L }

            if (latestMemoId != null) {
                currentMemoId = latestMemoId
                titleText = sharedPreferences.getString("memo_title_$latestMemoId", "") ?: ""
                memoText = sharedPreferences.getString("memo_content_$latestMemoId", "") ?: ""
                val savedBackgroundResId = sharedPreferences.getInt("memo_background_res_id_$latestMemoId", 0)

                if (savedBackgroundResId != 0) {
                    appliedImageResId = savedBackgroundResId
                    currentlySelectedImageResId = savedBackgroundResId
                }
            }
        }
    }

    val backgroundPainter = appliedImageResId?.let { painterResource(id = it) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                IconButton(
                    onClick = {
                        if (isImageApplied) {
                            isImageApplied = false
                            appliedImageResId = null
                        } else {
                            onBackClick()
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "뒤로가기",
                        tint = if (isImageApplied && backgroundPainter != null) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            if (backgroundPainter != null && isImageApplied) {
                Image(
                    painter = backgroundPainter,
                    contentDescription = "화면 배경 이미지",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            if (!isImageApplied) {
                // 이미지 선택 중 UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "이미지 선택",
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("현재 선택:")
                        if (currentlySelectedImageResId != null) {
                            Image(
                                painter = painterResource(id = currentlySelectedImageResId!!),
                                contentDescription = "현재 선택된 이미지 (미리보기)",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("선택된 이미지가 없습니다.")
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))

                        Text("아래에서 이미지를 선택하세요:")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            imageOptions.forEach { imageOption ->
                                Image(
                                    painter = painterResource(id = imageOption.resourceId),
                                    contentDescription = "선택 옵션 ${imageOption.id}",
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clickable {
                                            currentlySelectedImageResId = imageOption.resourceId
                                        }
                                        .then(
                                            if (currentlySelectedImageResId == imageOption.resourceId) {
                                                Modifier.border(
                                                    2.dp,
                                                    MaterialTheme.colorScheme.primary,
                                                    shape = MaterialTheme.shapes.medium
                                                )
                                            } else {
                                                Modifier.border(
                                                    1.dp,
                                                    MaterialTheme.colorScheme.outlineVariant,
                                                    MaterialTheme.shapes.medium
                                                )
                                            }
                                        )
                                        .padding(2.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            appliedImageResId = currentlySelectedImageResId
                            if (appliedImageResId != null) {
                                isImageApplied = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currentlySelectedImageResId != null
                    ) {
                        Text("배경으로 적용")
                    }
                }
            } else {
                // 이미지 적용 완료 UI (메모장 기능)
                val textColorOnBackgroundImage = MaterialTheme.colorScheme.onSurface
                val placeholderColorOnBackgroundImage = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                val cursorColorOnBackgroundImage = MaterialTheme.colorScheme.primary

                val titleTextStartX = 60.dp
                val titleTextStartY = (0).dp

                TextField(
                    value = titleText,
                    onValueChange = { newText -> titleText = newText },
                    modifier = Modifier
                        .offset(x = titleTextStartX, y = titleTextStartY)
                        .fillMaxWidth(0.9f),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        color = textColorOnBackgroundImage
                    ),
                    placeholder = {
                        Text("제목 입력", color = placeholderColorOnBackgroundImage)
                    },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = cursorColorOnBackgroundImage,
                        focusedTextColor = textColorOnBackgroundImage,
                        unfocusedTextColor = textColorOnBackgroundImage,
                        focusedPlaceholderColor = placeholderColorOnBackgroundImage,
                        unfocusedPlaceholderColor = placeholderColorOnBackgroundImage
                    ),
                )



                val contentTextStartX = 25.dp
                val titleFieldEstimatedHeight = 40.dp
                val spacingBetweenFields = 20.dp
                val contentTextStartY = titleTextStartY + titleFieldEstimatedHeight + spacingBetweenFields

                TextField(
                    value = memoText,
                    onValueChange = { newText -> memoText = newText },
                    modifier = Modifier
                        .offset(x = contentTextStartX, y = contentTextStartY)
                        .fillMaxWidth(0.9f),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = textColorOnBackgroundImage
                    ),
                    placeholder = {
                        Text("내용 입력", color = placeholderColorOnBackgroundImage)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = cursorColorOnBackgroundImage,
                        focusedTextColor = textColorOnBackgroundImage,
                        unfocusedTextColor = textColorOnBackgroundImage,
                        focusedPlaceholderColor = placeholderColorOnBackgroundImage,
                        unfocusedPlaceholderColor = placeholderColorOnBackgroundImage
                    ),
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height

                    val paddingHorizontal = 70.dp.toPx()  // 선 좌우 여백
                    val offsetUp = 295.dp.toPx()          // 선 세로 위치 이동
                    val centerY = canvasHeight / 2 - offsetUp

                    val lineStroke = 6.dp.toPx()
                    val dotRadius = lineStroke * 0.5f

                    // 점 위치 (독립적으로 설정 가능)
                    val dotX = paddingHorizontal / 2       // 점 X 위치
                    val dotY = centerY + 30.dp.toPx()      // 점 Y 위치 (선보다 위로)

                    drawCircle(
                        color = Color(0xFF28114B),
                        radius = dotRadius,
                        center = Offset(dotX, dotY)
                    )

                    // 선 위치
                    val lineStartX = paddingHorizontal
                    val lineEndX = canvasWidth - paddingHorizontal
                    val lineY = centerY                     // 선 세로 위치

                    drawLine(
                        color = Color(0xFF28114B),
                        start = Offset(lineStartX, lineY),
                        end = Offset(lineEndX, lineY),
                        strokeWidth = lineStroke
                    )
                }

                Button(
                    onClick = {
                        // 1. 현재 시간을 고유 ID로 사용
                        val currentTimeMillis = System.currentTimeMillis()
                        val memoId = currentTimeMillis.toString()

                        // 2. 개별 메모 저장 (고유 ID 사용)
                        val sharedPreferences = context.getSharedPreferences("MyMemoAppPrefs", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()

                        // 개별 메모 데이터 저장 (ID를 키로 사용)
                        editor.putString("memo_title_$memoId", titleText)
                        editor.putString("memo_content_$memoId", memoText)
                        editor.putInt("memo_background_res_id_$memoId", appliedImageResId ?: (imageOptions.firstOrNull()?.resourceId ?: 0))

                        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm", Locale.getDefault())
                        val currentDateAndTime: String = sdf.format(Date())
                        editor.putString("memo_save_date_$memoId", currentDateAndTime)

                        // 3. 메모 ID 리스트에 추가 (기존 리스트 가져와서 새 ID 추가)
                        val existingIds = sharedPreferences.getStringSet("memo_ids", mutableSetOf()) ?: mutableSetOf()
                        val updatedIds = existingIds.toMutableSet()

                        if (isEditMode && currentMemoId != null) {
                            // 편집 모드인 경우: 기존 메모를 업데이트
                            editor.putString("memo_title_$currentMemoId", titleText)
                            editor.putString("memo_content_$currentMemoId", memoText)
                            editor.putInt("memo_background_res_id_$currentMemoId", appliedImageResId ?: (imageOptions.firstOrNull()?.resourceId ?: 0))
                            editor.putString("memo_save_date_$currentMemoId", currentDateAndTime)

                            // 메인 화면용 데이터도 업데이트
                            editor.putString("memo_title", titleText)
                            editor.putString("memo_content", memoText)
                            editor.putInt("memo_background_res_id", appliedImageResId ?: (imageOptions.firstOrNull()?.resourceId ?: 0))
                            editor.putString("memo_save_date", currentDateAndTime)
                        } else {
                            // 새 메모 작성인 경우: 새 ID 추가
                            updatedIds.add(memoId)
                            editor.putStringSet("memo_ids", updatedIds)

                            // 새 메모이므로 메인 화면용 데이터도 업데이트
                            editor.putString("memo_title", titleText)
                            editor.putString("memo_content", memoText)
                            editor.putInt("memo_background_res_id", appliedImageResId ?: (imageOptions.firstOrNull()?.resourceId ?: 0))
                            editor.putString("memo_save_date", currentDateAndTime)
                        }

                        editor.apply() // 비동기 저장

                        println("메모 저장됨:")
                        println("메모 ID: $memoId")
                        println("제목: $titleText")
                        println("내용: $memoText")
                        println("전체 메모 개수: ${updatedIds.size}")

                        // 메인 화면에 성공 결과 전달
                        val resultIntent = Intent()
                        (context as? ComponentActivity)?.let { activity ->
                            activity.setResult(Activity.RESULT_OK, resultIntent)
                            activity.finish() // Activity 종료하여 메인으로 돌아가기
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Text(if (isEditMode) "메모 수정" else "메모 저장")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultNewMakeScreenPreview() {
    GreetingCardTheme {
        NewMakeScreen(isEditMode = false, onBackClick = {})
    }
}