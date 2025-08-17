package com.example.greetingcard

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.greetingcard.ui.theme.GreetingCardTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GreetingCardTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "남연하",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var menuExpanded by remember { mutableStateOf(false) }
    val menuItems = listOf("새로 만들기", "검색", "설정", "휴지통")

    // SharedPreferences에서 읽어온 제목을 저장할 상태 변수
    var displayedMemoTitle by remember { mutableStateOf<String?>(null) }
    val currentContext = LocalContext.current

    // SharedPreferences에서 데이터를 로드하는 함수
    val reloadSavedData: () -> Unit = {
        val sharedPreferences = currentContext.getSharedPreferences("MyMemoAppPrefs", Context.MODE_PRIVATE)
        val title = sharedPreferences.getString("memo_title", null)
        displayedMemoTitle = if (title.isNullOrEmpty()) null else title
        println("Greeting 화면 데이터 리로드됨: $displayedMemoTitle")
    }

    // Composable이 처음 실행될 때 데이터 로드
    LaunchedEffect(key1 = Unit) {
        reloadSavedData()
    }

    // newMake Activity를 시작하고 결과를 처리하는 Launcher
    val newMakeActivityLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            reloadSavedData() // newMake에서 돌아오면 데이터 다시 로드
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Transparent
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Image(
                painter = painterResource(id = R.drawable.croc),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 1000.dp, height = 600.dp)
                    .alpha(1f),
                contentScale = ContentScale.Fit
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
            {
                IconButton(
                    onClick = {
                        menuExpanded = !menuExpanded
                    },
                    modifier = Modifier
                        .size(100.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.face),
                        contentDescription = "메뉴 버튼",
                        modifier = Modifier.size(width = 100.dp, height = 100.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                modifier = Modifier.wrapContentSize(),
                offset = DpOffset(x = 230.dp, y = 300.dp)
            ) {
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.aaa),
                        contentDescription = "메뉴 배경",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        menuItems.forEach { menuItemText ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        println("$menuItemText 선택됨")
                                        menuExpanded = false
                                        when (menuItemText) {
                                            "새로 만들기" -> {
                                                // 수정: startActivity 대신 launcher 사용
                                                val intent = Intent(currentContext, newMake::class.java)
                                                newMakeActivityLauncher.launch(intent)
                                            }
                                            "검색" -> { /* 검색 로직 */
                                                val intent = Intent(currentContext, searMemo::class.java)
                                                currentContext.startActivity(intent)
                                            }
                                            "설정" -> { /* 설정 로직 */ }
                                            "휴지통" -> { /* 휴지통 로직 */ }
                                        }
                                    }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = menuItemText,
                                    color = Color(0xFF3617F1)
                                )
                            }
                        }
                    }
                }
            }

            if (displayedMemoTitle != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable {
                            val intent = Intent(currentContext, newMake::class.java)
                            intent.putExtra("EDIT_MODE", true) // 편집 모드임을 알림
                            newMakeActivityLauncher.launch(intent)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "최근 메모: $displayedMemoTitle",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }

            Text(
                text = "제작:남연하 그림:남무열",
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(25.dp),
                fontSize = 18.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GreetingCardTheme {
        Greeting("미리보기 테스트")
    }
}