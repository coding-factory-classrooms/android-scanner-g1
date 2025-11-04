## Architecture : MVVM + Atomic Design + Koin

### Structure de l'Architecture

```
View (Compose)
    ↓ observe StateFlow<UiState>
ViewModel
    ↓ use Repository
Repository
    ↓ use Service
Service → Android System
```

### Règles strictes

1. **View Layer** (`ui/screen/`, `ui/components/`)

   - Observe uniquement le `UiState` via `collectAsState()`
   - Appelle uniquement les fonctions publiques du ViewModel
   - Ne contient AUCUNE logique métier
   - Ne crée JAMAIS directement de Repository ou Service
   - Pas de `remember { mutableStateOf() }` pour l'état métier (uniquement pour l'état UI local comme `showBottomSheet`)

2. **ViewModel Layer** (`ui/viewmodel/`)

   - Expose UN SEUL `StateFlow<UiState>` (data class)
   - Utilise `_uiState.update { it.copy(...) }` pour toutes les mises à jour
   - Utilise `SharedFlow<UiEffect>` uniquement pour les effets uniques (navigation, snackbars)
   - Toutes les fonctions publiques sont `suspend` ou lancent des coroutines
   - Utilise `viewModelScope.launch` pour les opérations asynchrones
   - Pas de références directes aux Composables ou Activity

3. **Data Layer** (`data/`)
   - Repository : Interface publique + Impl qui utilise les Services
   - Service : Interface publique + Impl qui accède au système Android
   - Model : Data classes pures dans `data/model/`
   - Tous les résultats retournent `Result<T>` pour la gestion d'erreurs
   - Pas de logique métier complexe dans les Services

---

## Conventions de Code

### 1. UiState Pattern

**Toujours créer un seul UiState data class** :

```kotlin
data class UiState(
    val isRecording: Boolean = false,
    val recordedBase64: String? = null,
    val errorMessage: String? = null,
    // ... autres propriétés
)
```

**Mise à jour de l'état** :

```kotlin
// CORRECT
_uiState.update { it.copy(isRecording = true) }

//  INCORRECT
_uiState.value = _uiState.value.copy(isRecording = true)
_recordingState.value = RecordingState.Recording
```

### 2. Gestion des Erreurs

**Dans les Services/Repositories** :

```kotlin
// CORRECT
override fun doSomething(): Result<String> = runCatching {
    // Opération qui peut échouer
    "result"
}

// Utilisation dans ViewModel
repository.doSomething().fold(
    onSuccess = { result -> _uiState.update { it.copy(data = result) } },
    onFailure = { error -> _uiState.update { it.copy(errorMessage = error.message) } }
)
```

### 3. Composables et Références de Méthode

**Préférer les références de méthode** :

```kotlin
// CORRECT
onLanguageSelected = viewModel::selectLanguage
onStopClick = viewModel::stopRecording

//  INCORRECT
onLanguageSelected = { viewModel.selectLanguage(it) }
onStopClick = { viewModel.stopRecording() }
```

### 4. Design Atomique

**Atoms** (`ui/components/atoms/`) :

- Composants UI de base (Button, Text, Icon)
- Props minimales
- Aucune dépendance vers d'autres composants personnalisés

```kotlin
@Composable
fun MyAtom(text: String, modifier: Modifier = Modifier) {
    Text(text = text, modifier = modifier)
}
```

**Molecules** (`ui/components/molecules/`) :

- Combinaisons d'atoms
- Peuvent utiliser d'autres molecules
- Logique UI simple uniquement

```kotlin
@Composable
fun MyMolecule(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text("Label")
        TextField(value = value, onValueChange = onValueChange)
    }
}
```

**Organisms** (`ui/components/organisms/`) :

- Combinaisons de molecules et atoms
- Sections fonctionnelles complètes
- Peuvent contenir de la logique UI complexe

### 5. Injection de Dépendances avec Koin

**Configuration dans `di/appModule.kt`** :

```kotlin
val appModule = module {
    // Services : singleton
    single<MyService> { MyServiceImpl() }

    // Repositories : singleton, utilisent get() pour injection
    single<MyRepository> { MyRepositoryImpl(get()) }

    // ViewModels : créés à chaque besoin
    viewModel { MyViewModel(get()) }
}
```

**Utilisation dans Compose** :

```kotlin
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    // koinViewModel() injecte automatiquement
}
```

### 6. Nommage

**Fichiers et Classes** :

- ViewModels : `[Feature]ViewModel.kt` (ex: `AudioRecorderViewModel.kt`)
- Repositories : `[Feature]Repository.kt` + `[Feature]RepositoryImpl.kt`
- Services : `[Feature]Service.kt` + `[Feature]ServiceImpl.kt`
- Screens : `[Feature]Screen.kt` (ex: `AudioRecorderScreen.kt`)
- Composants : Nom descriptif (ex: `LanguageSelector.kt`, `RecordingControls.kt`)

**Variables et Fonctions** :

- UiState : `uiState: StateFlow<UiState>`
- MutableStateFlow privé : `_uiState: MutableStateFlow<UiState>`
- Fonctions ViewModel : verbes (ex: `startRecording()`, `stopRecording()`)
- Props Composable : nom descriptif (ex: `selectedLanguage`, `onLanguageSelected`)

---

## Patterns à Suivre

### Pattern 1 : Nouvelle Fonctionnalité

1. **Data Model** → `data/model/MyModel.kt`
2. **Service** → `data/service/MyService.kt` (interface) + `MyServiceImpl.kt`
3. **Repository** → `data/repository/MyRepository.kt` (interface) + `MyRepositoryImpl.kt`
4. **ViewModel** → `ui/viewmodel/MyViewModel.kt` avec `UiState` data class
5. **Composants UI** → `ui/components/atoms|molecules|organisms/`
6. **Screen** → `ui/screen/MyScreen.kt`
7. **Koin Module** → Ajouter dans `di/appModule.kt`

### Pattern 2 : Ajout d'un État dans UiState

```kotlin
// 1. Ajouter dans UiState
data class UiState(
    val existingProperty: String = "",
    val newProperty: Int = 0,  // ← Nouveau
    // ...
)

// 2. Mettre à jour dans ViewModel
_uiState.update { it.copy(newProperty = 42) }

// 3. Utiliser dans View
val uiState by viewModel.uiState.collectAsState()
Text(text = "${uiState.newProperty}")
```

### Pattern 3 : Gestion des Permissions

```kotlin
// Dans le Screen (View)
val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.XXX) == PackageManager.PERMISSION_GRANTED

val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    viewModel.onPermissionResult(isGranted)
}

// Dans le ViewModel
fun onPermissionResult(isGranted: Boolean) {
    if (isGranted) {
        // Action après permission accordée
    } else {
        _uiState.update { it.copy(errorMessage = "Permission refusée") }
    }
}
```

### Pattern 4 : Side Effects (Navigation, Snackbars)

```kotlin
// ViewModel
sealed class UiEffect {
    object NavigateToScreen : UiEffect()
    data class ShowSnackbar(val message: String) : UiEffect()
}

private val _uiEffect = MutableSharedFlow<UiEffect>()
val uiEffect: SharedFlow<UiEffect> = _uiEffect.asSharedFlow()

private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.emit(effect) }

// View
LaunchedEffect(viewModel.uiEffect) {
    viewModel.uiEffect.collect { effect ->
        when (effect) {
            is UiEffect.NavigateToScreen -> { /* navigation */ }
            is UiEffect.ShowSnackbar -> { /* snackbar */ }
        }
    }
}
```

---

## Anti-Patterns (À ÉVITER)

### Plusieurs StateFlows dans le ViewModel

```kotlin
//  MAUVAIS
val recordingState: StateFlow<RecordingState>
val amplitude: StateFlow<Int>
val duration: StateFlow<Long>

// BON
data class UiState(
    val isRecording: Boolean,
    val amplitude: Int,
    val duration: Long
)
val uiState: StateFlow<UiState>
```

### Logique métier dans la View

```kotlin
//  MAUVAIS
@Composable
fun MyScreen() {
    val data = remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        // Appel direct à un repository ou service
        repository.getData()
    }
}

// BON
@Composable
fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // UI basée sur uiState
}
```

### Création manuelle des dépendances

```kotlin
//  MAUVAIS
class MyViewModel : ViewModel() {
    private val repository = AudioRepositoryImpl(AudioRecorderServiceImpl())
}

// BON
class MyViewModel(
    private val repository: AudioRepository  // Injecté par Koin
) : ViewModel()
```

### Mise à jour directe du StateFlow

```kotlin
//  MAUVAIS
_uiState.value = _uiState.value.copy(isRecording = true)

// BON
_uiState.update { it.copy(isRecording = true) }
```

---

## Checklist pour Nouveau Code

Avant de créer du code, vérifier :

- [ ] Le ViewModel expose un seul `StateFlow<UiState>`
- [ ] Les mises à jour utilisent `update { it.copy(...) }`
- [ ] Les Services/Repositories retournent `Result<T>`
- [ ] Les Composables utilisent `viewModel::function` pour les callbacks
- [ ] Les composants sont dans le bon dossier (atoms/molecules/organisms)
- [ ] Toutes les dépendances sont injectées via Koin
- [ ] Les erreurs sont gérées dans le ViewModel, pas dans la View
- [ ] Le code est testable (pas de références Android dans ViewModel)
- [ ] Les noms sont clairs et descriptifs

---

## Exemples de Code Type

### ViewModel Type

```kotlin
class FeatureViewModel(
    private val repository: FeatureRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<UiEffect>()
    val uiEffect: SharedFlow<UiEffect> = _uiEffect.asSharedFlow()

    fun performAction() {
        viewModelScope.launch {
            repository.doSomething().fold(
                onSuccess = { result ->
                    _uiState.update { it.copy(data = result) }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(errorMessage = error.message) }
                }
            )
        }
    }

    data class UiState(
        val data: String? = null,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    sealed class UiEffect {
        object NavigateBack : UiEffect()
    }
}
```

### Screen Type

```kotlin
@Composable
fun FeatureScreen(
    viewModel: FeatureViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel.uiEffect) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is FeatureViewModel.UiEffect.NavigateBack -> { /* navigation */ }
            }
        }
    }

    Column(modifier = modifier) {
        if (uiState.isLoading) {
            CircularProgressIndicator()
        } else {
            uiState.data?.let { data ->
                Text(text = data)
            }
        }

        uiState.errorMessage?.let { error ->
            ErrorText(text = error)
        }

        Button(onClick = viewModel::performAction) {
            Text("Action")
        }
    }
}
```

---

## Notes Importantes

1. **Un seul UiState** : C'est la règle la plus importante. Ne jamais créer plusieurs StateFlows.
2. **Pas de logique dans la View** : La View observe et affiche uniquement.
3. **Result<T> pour les erreurs** : Toujours utiliser `Result<T>` dans les Services/Repositories.
4. **Koin pour l'injection** : Ne jamais créer manuellement les dépendances.
5. **Atomic Design** : Respecter la hiérarchie atoms → molecules → organisms.

---

## Questions Fréquentes

**Q : Où mettre l'état local UI (comme `showBottomSheet`) ?**
R : Dans le Composable avec `remember { mutableStateOf() }`. C'est acceptable car c'est un état UI purement local.

**Q : Quand utiliser SharedFlow vs StateFlow ?**
R : StateFlow pour l'état UI (observé dans la View), SharedFlow pour les effets uniques (navigation, snackbars).

**Q : Comment tester le ViewModel ?**
R : Mock les Repositories injectés, vérifier les mises à jour de `uiState`.

**Q : Peut-on avoir plusieurs ViewModels dans un Screen ?**
R : Oui, mais éviter si possible. Préférer un ViewModel par feature.

---
