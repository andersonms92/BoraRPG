package com.borarpg

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.borarpg.data.*
import com.borarpg.ui.AdventureViewModel

class MainActivity : ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val app = applicationContext
  val factory = object : ViewModelProvider.Factory {
   override fun <T : ViewModel> create(modelClass: Class<T>): T {
    require(modelClass.isAssignableFrom(AdventureViewModel::class.java))
    val db = DatabaseProvider.get(app)
    @Suppress("UNCHECKED_CAST")
    return AdventureViewModel(AdventureRepository(db)) as T
   }
  }
  setContent {
   MaterialTheme(colorScheme = darkColorScheme(primary = Color(0xFFA6E575), background = Color(0xFF111B18), surface = Color(0xFF1C2B25))) {
    val vm: AdventureViewModel = viewModel(factory = factory)
    val state by vm.state.collectAsStateWithLifecycle()
    var tab by remember { mutableIntStateOf(0) }
    Scaffold(bottomBar = {
     NavigationBar { listOf("Jornada", "Personagem", "Inventário").forEachIndexed { index, title ->
      NavigationBarItem(selected = tab == index, onClick = { tab = index }, icon = { Text(listOf("⚔", "★", "▣")[index]) }, label = { Text(title) })
     } }
    }) { padding ->
     Column(Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
      Text("BoraRPG", style = MaterialTheme.typography.headlineLarge)
      Text("Cada movimento avança sua história.")
      if (state.loading) { CircularProgressIndicator() } else {
       val hero = state.hero
       when (tab) {
        0 -> {
         Text("FLORESTA DO RECOMEÇO", color = MaterialTheme.colorScheme.primary)
         Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Guardião das raízes", style = MaterialTheme.typography.headlineSmall)
          Text("Vida: ${hero.hp} / 1000")
          LinearProgressIndicator(progress = hero.hp / 1000f, modifier = Modifier.fillMaxWidth())
          Text("Missão: cause 1.000 de dano com seu movimento. O combate continua na próxima sessão.")
          Text("Recompensa: 50 XP extras, 25 de ouro e um fragmento da floresta.")
         } }
         Button(onClick = vm::simulate, modifier = Modifier.fillMaxWidth()) { Text("Simular 250 passos") }
         Text("PROTÓTIPO • Passos simulados. Sensores, GPS e registro de bicicleta ainda não estão conectados.", style = MaterialTheme.typography.bodySmall)
         Text("Vitórias: ${hero.victories}  •  Passos de teste: ${hero.steps}")
        }
        1 -> {
         Text("Explorador · Nível ${hero.level}", style = MaterialTheme.typography.headlineMedium)
         Text("Experiência total: ${hero.xp} XP")
         LinearProgressIndicator(progress = (hero.xp % 100) / 100f, modifier = Modifier.fillMaxWidth())
         Text("${100 - hero.xp % 100} XP para o próximo nível")
         Text("Ouro: ${hero.gold}")
         Text("Sua jornada permanece salva neste aparelho. Volte no seu ritmo.")
        }
        2 -> {
         Text("Sua mochila", style = MaterialTheme.typography.headlineMedium)
         Text("Espada de madeira · equipamento inicial")
         Text("Fragmentos da floresta: ${hero.victories}")
         Text("Ouro disponível: ${hero.gold}")
         Text("Loja e efeitos dos equipamentos serão adicionados em outra etapa.")
        }
       }
      }
      state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
     }
    }
   }
  }
 }
}
private object DatabaseProvider {
 @Volatile private var instance: GameDatabase? = null
 fun get(context: android.content.Context): GameDatabase = instance ?: synchronized(this) {
  instance ?: Room.databaseBuilder(context.applicationContext, GameDatabase::class.java, "borarpg.db").build().also { instance = it }
 }
}