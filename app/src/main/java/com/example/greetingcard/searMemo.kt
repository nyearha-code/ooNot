package com.example.greetingcard

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.greetingcard.ui.theme.GreetingCardTheme


// 메모 데이터 클래스
data class MemoItem(
    val id: String, // 메모의 고유 ID 추가
    val title: String,
    val content: String,
    val saveDate: String,
    val backgroundResId: Int
)

class searMemo : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GreetingCardTheme {
                SearchMemoScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchMemoScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current


    // 검색어 상태
    var searchText by remember { mutableStateOf("") }

    // 전체 메모 리스트
    var allMemos by remember { mutableStateOf<List<MemoItem>>(emptyList()) }

    // 필터된 메모 리스트
    val filteredMemos = remember(allMemos, searchText) {
        if (searchText.isEmpty()) {
            allMemos
        } else {
            allMemos.filter { memo ->
                memo.title.contains(searchText, ignoreCase = true) ||
                        memo.content.contains(searchText, ignoreCase = true)
            }
        }
    }

    // 선택된 메모 (편집용)
    var selectedMemo by remember { mutableStateOf<MemoItem?>(null) }

    // 삭제 확인 다이얼로그 표시 상태
    var showDeleteDialog by remember { mutableStateOf(false) }
    var memoToDelete by remember { mutableStateOf<MemoItem?>(null) }

    // SharedPreferences에서 메모 데이터 로드
    LaunchedEffect(Unit) {
        val sharedPreferences = context.getSharedPreferences("MyMemoAppPrefs", Context.MODE_PRIVATE)

        // 모든 메모 ID 가져오기
        val memoIds = sharedPreferences.getStringSet("memo_ids", emptySet()) ?: emptySet()

        val memoList = mutableListOf<MemoItem>()

        // 각 메모 ID에 대해 데이터 로드
        for (memoId in memoIds) {
            val title = sharedPreferences.getString("memo_title_$memoId", "") ?: ""
            val content = sharedPreferences.getString("memo_content_$memoId", "") ?: ""
            val saveDate = sharedPreferences.getString("memo_save_date_$memoId", "") ?: ""
            val backgroundResId = sharedPreferences.getInt("memo_background_res_id_$memoId", 0)

            if (title.isNotEmpty() || content.isNotEmpty()) {
                memoList.add(
                    MemoItem(
                        id = memoId, // ID 추가
                        title = if (title.isEmpty()) "제목 없음" else title,
                        content = content,
                        saveDate = saveDate,
                        backgroundResId = backgroundResId
                    )
                )
            }
        }

        // 날짜순으로 정렬 (최신순)
        allMemos = memoList.sortedByDescending { memo ->
            try {
                // 저장된 날짜 형식을 파싱하여 정렬
                val sdf =
                    java.text.SimpleDateFormat("yyyy.MM.dd HH:mm", java.util.Locale.getDefault())
                sdf.parse(memo.saveDate)?.time ?: 0L
            } catch (e: Exception) {
                0L
            }
        }

        println("로드된 메모 개수: ${allMemos.size}")
        allMemos.forEach { memo ->
            println("메모: ${memo.title} - ${memo.saveDate}")
        }
    }

    // 메모 삭제 함수
    fun deleteMemo(memo: MemoItem) {
        val sharedPreferences = context.getSharedPreferences("MyMemoAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // 개별 메모 데이터 삭제
        editor.remove("memo_title_${memo.id}")
        editor.remove("memo_content_${memo.id}")
        editor.remove("memo_save_date_${memo.id}")
        editor.remove("memo_background_res_id_${memo.id}")

        // 메모 ID 목록에서 제거
        val memoIds = sharedPreferences.getStringSet("memo_ids", emptySet())?.toMutableSet() ?: mutableSetOf()
        memoIds.remove(memo.id)
        editor.putStringSet("memo_ids", memoIds)

        editor.apply()

        // UI에서도 제거
        allMemos = allMemos.filter { it.id != memo.id }

        println("메모 삭제 완료: ${memo.title}")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("메모 검색") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { innerPadding ->
        if (selectedMemo == null) {
            // 메모 리스트 화면
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFA39AB6)) // 연한 보라색
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                // 검색창
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    label = { Text("메모 검색") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "검색")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true
                )

                // 메모 개수 표시
                Text(
                    text = if (searchText.isEmpty()) {
                        "전체 메모: ${allMemos.size}개"
                    } else {
                        "검색 결과: ${filteredMemos.size}개"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 메모 리스트
                if (filteredMemos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchText.isEmpty()) "저장된 메모가 없습니다." else "검색 결과가 없습니다.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredMemos) { memo ->
                            MemoListItem(
                                memo = memo,
                                onClick = { selectedMemo = memo },// 바로 편집 모드로
                                onDeleteClick = {
                                    memoToDelete = memo
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        } else {
            // 메모 편집 화면 (바로 편집 모드)
            MemoEditScreen(
                memo = selectedMemo!!,
                onBackClick = { selectedMemo = null },
                onMemoUpdated = { updatedMemo ->
                    // 메모가 업데이트되면 리스트도 업데이트
                    allMemos = allMemos.map { memo ->
                        if (memo.id == updatedMemo.id) updatedMemo else memo
                    }
                    selectedMemo = null // 편집 완료 후 리스트로 돌아가기
                }
            )
        }
    }

                    // 삭제 확인 다이얼로그
    if (showDeleteDialog && memoToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                memoToDelete = null
            },
            title = { Text("메모 삭제") },
            text = { Text("'${memoToDelete!!.title}' 메모를 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteMemo(memoToDelete!!)
                        showDeleteDialog = false
                        memoToDelete = null
                    }
                ) {
                    Text("삭제", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        memoToDelete = null
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}

    @Composable
    fun MemoListItem(
        memo: MemoItem,
        onClick: () -> Unit,
        onDeleteClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onClick() }
        ) {
            // 배경 이미지
            memo.backgroundResId?.let { resId ->
                Image(
                    painter = painterResource(id = resId),
                    contentDescription = "메모 배경",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 텍스트 (제목 + 날짜) — 배경 투명하게
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = memo.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black  // 배경이 밝으면 검정색, 어두우면 흰색으로 조절
                )
                Text(
                    text = memo.saveDate ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Black
                )
            }

            // 삭제 버튼
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "삭제",
                    tint = Color.Red // 삭제 버튼은 눈에 띄게
                )
            }
        }
    }


@OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MemoEditScreen(
        memo: MemoItem,
        onBackClick: () -> Unit,
        onMemoUpdated: (MemoItem) -> Unit
    ) {
        val context = LocalContext.current

        // 편집 가능한 텍스트 상태 (바로 편집 모드로 시작)
        var editableTitle by remember { mutableStateOf(memo.title) }
        var editableContent by remember { mutableStateOf(memo.content) }

        // 자동 저장 함수
        fun autoSaveMemo() {
            val sharedPreferences =
                context.getSharedPreferences("MyMemoAppPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            // 개별 메모 데이터 업데이트
            editor.putString("memo_title_${memo.id}", editableTitle)
            editor.putString("memo_content_${memo.id}", editableContent)

            // 저장 날짜 업데이트
            val sdf = java.text.SimpleDateFormat(
                "yyyy.MM.dd HH:mm",
                java.util.Locale.getDefault()
            )
            val currentDateAndTime: String = sdf.format(java.util.Date())
            editor.putString("memo_save_date_${memo.id}", currentDateAndTime)

            // 만약 이 메모가 가장 최근 메모라면 메인 화면용 데이터도 업데이트
            val memoIds =
                sharedPreferences.getStringSet("memo_ids", emptySet()) ?: emptySet()
            val latestMemoId = memoIds.maxByOrNull { it.toLongOrNull() ?: 0L }
            if (memo.id == latestMemoId) {
                editor.putString("memo_title", editableTitle)
                editor.putString("memo_content", editableContent)
                editor.putString("memo_save_date", currentDateAndTime)
            }

            editor.apply()

            // 업데이트된 메모 객체 생성
            val updatedMemo = memo.copy(
                title = editableTitle,
                content = editableContent,
                saveDate = currentDateAndTime
            )

            // 상위 컴포넌트에 업데이트 알림
            onMemoUpdated(updatedMemo)

            println("메모 자동 저장 완료: ${updatedMemo.title}")
        }

        // 화면에서 벗어날 때 자동 저장
        DisposableEffect(Unit) {
            onDispose {
                // 내용이 변경되었을 때만 저장
                if (editableTitle != memo.title || editableContent != memo.content) {
                    autoSaveMemo()
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // 상단 버튼들
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 뒤로가기 버튼과 제목
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = {
                        // 뒤로가기 시 자동 저장
                        if (editableTitle != memo.title || editableContent != memo.content) {
                            autoSaveMemo()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "목록으로 돌아가기")
                    }
                    Text(
                        text = "메모 편집",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 메모 편집 카드
            Card(
                modifier = Modifier.fillMaxSize(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = memo.backgroundResId),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // 🔹 배경 이미지
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                    ) {
                        // newMake.kt의 스타일을 참조하기 위한 변수 (필요한 경우 SharedMemo의 Composable 함수 내에 선언)
                        val textColorOnBackgroundImage = MaterialTheme.colorScheme.onSurface
                        val placeholderColorOnBackgroundImage =
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        val cursorColorOnBackgroundImage = MaterialTheme.colorScheme.primary

                        TextField(
                            value = editableTitle,
                            onValueChange = { editableTitle = it },
                            modifier = Modifier
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
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = cursorColorOnBackgroundImage,
                                focusedTextColor = textColorOnBackgroundImage,
                                unfocusedTextColor = textColorOnBackgroundImage,
                                focusedPlaceholderColor = placeholderColorOnBackgroundImage,
                                unfocusedPlaceholderColor = placeholderColorOnBackgroundImage
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp)) // 간격은 유지하거나 필요에 따라 조정
                        TextField(
                            value = editableContent,
                            onValueChange = { editableContent = it },
                            modifier = Modifier
                                .fillMaxWidth(0.9f) // newMake.kt와 유사하게 너비 조정 (필요에 따라 값 변경)
                            ,
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = textColorOnBackgroundImage // 텍스트 색상 적용
                            ),
                            placeholder = { // label 대신 placeholder 사용 및 스타일 적용
                                Text("내용 입력", color = placeholderColorOnBackgroundImage)
                            },
                            colors = TextFieldDefaults.colors( // newMake.kt와 동일한 colors 설정
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                errorContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent, // 밑줄 제거
                                unfocusedIndicatorColor = Color.Transparent, // 밑줄 제거
                                cursorColor = cursorColorOnBackgroundImage,
                                focusedTextColor = textColorOnBackgroundImage,
                                unfocusedTextColor = textColorOnBackgroundImage,
                                focusedPlaceholderColor = placeholderColorOnBackgroundImage,
                                unfocusedPlaceholderColor = placeholderColorOnBackgroundImage
                            )
                        )

                        // 원본 저장 날짜 표시
                        if (memo.saveDate.isNotEmpty()) {
                            Text(
                                text = "저장일: ${memo.saveDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun SearchMemoScreenPreview() {
        GreetingCardTheme {
            SearchMemoScreen(onBackClick = {})
        }
    }
