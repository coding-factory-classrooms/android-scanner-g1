# Architecture du Projet - Android Scanner

## Table des matières

- [Vue d'ensemble](#vue-densemble)
- [Architecture MVVM](#architecture-mvvm)
- [Design Atomique (Atomic Design)](#design-atomique-atomic-design)
- [Injection de Dépendances (Koin)](#injection-de-dépendances-koin)
- [Data Layer](#data-layer)
- [Bonnes Pratiques](#bonnes-pratiques)
- [Structure des Fichiers](#structure-des-fichiers)

---

## Vue d'ensemble

Ce projet Android utilise une architecture **MVVM (Model-View-ViewModel)** combinée avec le **Design Atomique** pour l'UI et **Koin** pour l'injection de dépendances. L'application permet d'enregistrer des fichiers audio et de les encoder en Base64 pour la transcription.

### Technologies principales

- **Kotlin** : Langage de programmation
- **Jetpack Compose** : Framework UI moderne
- **Koin** : Framework d'injection de dépendances
- **StateFlow / SharedFlow** : Gestion réactive de l'état
- **Coroutines** : Programmation asynchrone

---

## Architecture MVVM

L'architecture MVVM sépare l'application en trois couches principales :

```
┌─────────────────────────────────────┐
│          View (UI Layer)            │
│  - Composables Compose              │
│  - Screens                          │
│  - Atomic Components                │
└──────────────┬──────────────────────┘
               │ observe
               ▼
┌─────────────────────────────────────┐
│      ViewModel (Logic Layer)        │
│  - UiState (StateFlow)              │
│  - Business Logic                   │
│  - State Management                 │
└──────────────┬──────────────────────┘
               │ use
               ▼
┌─────────────────────────────────────┐
│    Data Layer                       │
│  - Repository                       │
│  - Service                          │
│  - Model                            │
└─────────────────────────────────────┘
```

### 1. View (Couche Présentation)

**Rôle** : Afficher les données et capturer les interactions utilisateur.

**Localisation** : `ui/screen/`, `ui/components/`

**Responsabilités** :

- Observer le `UiState` du ViewModel
- Afficher l'UI basée sur l'état
- Appeler les fonctions du ViewModel pour les actions utilisateur
- **NE PAS** contenir de logique métier
- **NE PAS** accéder directement aux repositories ou services

**Exemple** :

```kotlin
@Composable
fun AudioRecorderScreen(
    viewModel: AudioRecorderViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    // Afficher l'UI basée sur uiState
    // Appeler viewModel.startRecording() pour les actions
}
```

### 2. ViewModel (Couche Logique)

**Rôle** : Gérer l'état UI et la logique métier.

**Localisation** : `ui/viewmodel/`

**Responsabilités** :

- Exposer un seul `UiState` (data class) via `StateFlow`
- Exposer des fonctions publiques pour les actions utilisateur
- Utiliser les repositories pour accéder aux données
- Gérer les coroutines avec `viewModelScope`
- Émettre des effets via `SharedFlow` si nécessaire

**Structure du UiState** :

```kotlin
data class UiState(
    val isRecording: Boolean = false,
    val recordedBase64: String? = null,
    val amplitude: Int = 0,
    val recordingDuration: Long = 0L,
    val selectedLanguage: String = "fr-FR",
    val errorMessage: String? = null
)
```

**Principes** :

- **Un seul StateFlow** : Un seul `uiState: StateFlow<UiState>` expose tout l'état
- **Mise à jour atomique** : Utiliser `_uiState.update { it.copy(...) }` pour les modifications
- **Pas de référence Android** : Le ViewModel ne doit pas référencer directement les Views

**Exemple** :

```kotlin
class AudioRecorderViewModel(
    private val audioRepository: AudioRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun startRecording() {
        viewModelScope.launch {
            audioRepository.startRecording(...).fold(
                onSuccess = { _uiState.update { it.copy(isRecording = true) } },
                onFailure = { _uiState.update { it.copy(errorMessage = it.message) } }
            )
        }
    }
}
```

### 3. Data Layer (Couche Données)

**Rôle** : Gérer l'accès aux données et la logique de persistance.

**Localisation** : `data/`

**Structure** :

```
data/
├── model/          # Modèles de données (data classes)
├── repository/     # Interfaces et implémentations des repositories
└── service/        # Services pour les opérations système (audio, réseau, etc.)
```

#### Repository Pattern

**Rôle** : Abstraction pour l'accès aux données.

**Responsabilités** :

- Définir l'interface publique pour accéder aux données
- Coordonner entre plusieurs sources de données si nécessaire
- Gérer les transformations de données

**Exemple** :

```kotlin
interface AudioRepository {
    suspend fun startRecording(context: Context, fileName: String): Result<File>
    suspend fun stopRecording(): Result<String>
    fun isRecording(): Boolean
    fun getCurrentAmplitude(): Int
}

class AudioRepositoryImpl(
    private val audioRecorderService: AudioRecorderService
) : AudioRepository {
    override suspend fun startRecording(context: Context, fileName: String) =
        runCatching { audioRecorderService.startRecording(context, fileName).getOrThrow() }
}
```

#### Service Pattern

**Rôle** : Encapsuler les opérations système (MediaRecorder, API, etc.).

**Responsabilités** :

- Gérer les interactions avec les APIs système Android
- Gérer les erreurs techniques
- Retourner `Result<T>` pour la gestion d'erreurs

**Exemple** :

```kotlin
interface AudioRecorderService {
    fun startRecording(context: Context, fileName: String): Result<File>
    fun stopRecording(): Result<String>
    fun isRecording(): Boolean
}

class AudioRecorderServiceImpl : AudioRecorderService {
    private var mediaRecorder: MediaRecorder? = null

    override fun startRecording(context: Context, fileName: String): Result<File> = runCatching {
        // Logique d'enregistrement audio
    }
}
```

#### Model

**Rôle** : Définir les structures de données.

**Localisation** : `data/model/`

**Exemple** :

```kotlin
data class Language(val code: String, val name: String)

val availableLanguages = listOf(
    Language("fr-FR", "Français"),
    Language("en-US", "English"),
    // ...
)
```

---

## Design Atomique (Atomic Design)

Le design atomique organise les composants UI en 5 niveaux de complexité croissante :

```
Atoms (Plus petit)
    ↓
Molecules
    ↓
Organisms
    ↓
Templates
    ↓
Pages (Plus complexe)
```

### Structure dans le projet

```
ui/components/
├── atoms/          # Composants de base (Button, Text, Icon, etc.)
├── molecules/      # Combinaisons d'atoms (TextField + Icon, Card + Text, etc.)
└── organisms/      # Sections complexes (Form, Header, Navigation, etc.)
```

### 1. Atoms

**Rôle** : Composants UI de base, non divisibles.

**Exemples** : `ScreenTitle`, `ErrorText`, `RecordIconButton`, `StopIconButton`

**Caractéristiques** :

- Composant unique et réutilisable
- Ne dépend d'aucun autre composant personnalisé
- Props minimales et claires

**Exemple** :

```kotlin
@Composable
fun ScreenTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}
```

### 2. Molecules

**Rôle** : Combinaisons simples d'atoms formant une fonctionnalité.

**Exemples** : `DurationDisplay`, `AmplitudeVisualizer`, `LanguageSelector`, `RecordedFileCard`

**Caractéristiques** :

- Combine plusieurs atoms
- Réutilisable dans différents contextes
- Logique UI simple (pas de logique métier)

**Exemple** :

```kotlin
@Composable
fun DurationDisplay(duration: Long, modifier: Modifier = Modifier) {
    Text(
        text = formatDuration(duration),
        style = MaterialTheme.typography.titleLarge,
        modifier = modifier
    )
}
```

### 3. Organisms

**Rôle** : Sections complexes combinant molecules et atoms.

**Exemples** : `RecordingControls`, `RecordingStatus`

**Caractéristiques** :

- Combine molecules et atoms
- Représente une section fonctionnelle complète
- Peut contenir de la logique UI complexe

**Exemple** :

```kotlin
@Composable
fun RecordingControls(
    isRecording: Boolean,
    duration: Long,
    amplitude: Int,
    onRecordClick: () -> Unit,
    onStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        if (isRecording) {
            AmplitudeVisualizer(amplitude = amplitude)
            DurationDisplay(duration = duration)
            StopIconButton(onClick = onStopClick)
        } else {
            RecordIconButton(onClick = onRecordClick)
        }
    }
}
```

### 4. Pages / Screens

**Rôle** : Écrans complets de l'application.

**Localisation** : `ui/screen/`

**Caractéristiques** :

- Compose plusieurs organisms
- Connecte la View au ViewModel
- Gère les permissions et la navigation

---

## Injection de Dépendances (Koin)

**Koin** est utilisé pour l'injection de dépendances, facilitant les tests et la modularité.

### Configuration

**Localisation** : `di/appModule.kt`

**Exemple** :

```kotlin
val appModule = module {
    // Singleton pour les services
    single<AudioRecorderService> { AudioRecorderServiceImpl() }

    // Singleton pour les repositories
    single<AudioRepository> { AudioRepositoryImpl(get()) }

    // ViewModel (créé à chaque besoin)
    viewModel { AudioRecorderViewModel(get(), get()) }
}
```

### Initialisation

**Localisation** : `ScannerApplication.kt`

```kotlin
class ScannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ScannerApplication)
            modules(appModule)
        }
    }
}
```

### Utilisation dans Compose

```kotlin
@Composable
fun AudioRecorderScreen(
    viewModel: AudioRecorderViewModel = koinViewModel()
) {
    // Le ViewModel est automatiquement injecté par Koin
}
```

### Avantages

- **Testabilité** : Facile de mocker les dépendances
- **Découplage** : Les classes ne créent pas leurs dépendances
- **Maintenabilité** : Changement de dépendances centralisé

---

## Data Layer

### Gestion d'erreurs

Utiliser `Result<T>` pour encapsuler les succès et erreurs :

```kotlin
interface AudioRepository {
    suspend fun startRecording(context: Context, fileName: String): Result<File>
    suspend fun stopRecording(): Result<String>
}

// Utilisation
audioRepository.startRecording(context, fileName).fold(
    onSuccess = { file -> /* Traiter le succès */ },
    onFailure = { error -> /* Traiter l'erreur */ }
)
```

### Services et Repositories

**Services** : Interagissent directement avec les APIs système (MediaRecorder, etc.)
**Repositories** : Abstraction au-dessus des services, peuvent combiner plusieurs sources

**Principe** :

- ViewModel → Repository → Service → Système Android

---

## Bonnes Pratiques

### MVVM

1. **Un seul UiState** : Exposer un seul `StateFlow<UiState>` au lieu de plusieurs StateFlows
2. **Pas de logique dans la View** : La View observe et affiche uniquement
3. **StateFlow pour l'état** : Utiliser `StateFlow` pour l'état UI observable
4. **SharedFlow pour les effets** : Utiliser `SharedFlow` pour les événements uniques (snackbars, navigation)
5. **Mise à jour atomique** : Utiliser `update { it.copy(...) }` pour modifier l'état

### Design Atomique

1. **Réutilisabilité** : Créer des composants réutilisables
2. **Séparation des responsabilités** : Chaque composant a un rôle clair
3. **Composition** : Construire les composants complexes à partir des simples
4. **Props minimales** : Ne passer que les props nécessaires

### Code Quality

1. **Fonctions pures** : Les Composables doivent être idempotents
2. **Nommage clair** : Noms explicites pour les fonctions et variables
3. **Immutabilité** : Utiliser `copy()` pour les data classes
4. **Gestion d'erreurs** : Toujours gérer les erreurs avec `Result<T>` ou `try-catch`

### Kotlin

1. **Références de méthode** : Utiliser `viewModel::function` au lieu de `{ viewModel.function() }`
2. **Extension functions** : Créer des fonctions d'extension pour améliorer la lisibilité
3. **Data classes** : Utiliser pour les modèles et UiState
4. **Sealed classes** : Pour les états limités (succès, erreur, chargement)

---

## Structure des Fichiers

```
app/src/main/java/com/example/scanner/
├── MainActivity.kt                    # Point d'entrée de l'application
├── ScannerApplication.kt              # Application class (initialisation Koin)
│
├── data/
│   ├── model/
│   │   └── Language.kt               # Modèles de données
│   ├── repository/
│   │   └── AudioRepository.kt        # Interfaces et implémentations
│   └── service/
│       └── AudioRecorderService.kt   # Services système
│
├── di/
│   └── appModule.kt                  # Configuration Koin
│
├── ui/
│   ├── components/
│   │   ├── atoms/                    # Composants de base
│   │   │   ├── ScreenTitle.kt
│   │   │   ├── ErrorText.kt
│   │   │   ├── RecordIconButton.kt
│   │   │   └── ...
│   │   ├── molecules/                # Combinaisons d'atoms
│   │   │   ├── DurationDisplay.kt
│   │   │   ├── AmplitudeVisualizer.kt
│   │   │   ├── LanguageSelector.kt
│   │   │   └── ...
│   │   └── organisms/                # Sections complexes
│   │       ├── RecordingControls.kt
│   │       └── RecordingStatus.kt
│   │
│   ├── screen/
│   │   └── AudioRecorderScreen.kt    # Écrans de l'application
│   │
│   ├── theme/
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   └── Type.kt
│   │
│   └── viewmodel/
│       └── AudioRecorderViewModel.kt  # ViewModels
```

---

## Flux de Données

```
1. User Action (Click)
   ↓
2. View appelle viewModel.function()
   ↓
3. ViewModel appelle repository.function()
   ↓
4. Repository appelle service.function()
   ↓
5. Service retourne Result<T>
   ↓
6. Repository retourne Result<T>
   ↓
7. ViewModel met à jour _uiState.update { ... }
   ↓
8. View observe uiState et se recompose automatiquement
```

---

## Exemple Complet

### Ajouter une nouvelle fonctionnalité

1. **Créer le Model** (`data/model/`)

   ```kotlin
   data class MyModel(val id: String, val name: String)
   ```

2. **Créer le Service** (`data/service/`)

   ```kotlin
   interface MyService {
       suspend fun doSomething(): Result<MyModel>
   }
   ```

3. **Créer le Repository** (`data/repository/`)

   ```kotlin
   interface MyRepository {
       suspend fun getData(): Result<MyModel>
   }
   ```

4. **Créer le ViewModel** (`ui/viewmodel/`)

   ```kotlin
   class MyViewModel(private val repository: MyRepository) : ViewModel() {
       private val _uiState = MutableStateFlow(MyUiState())
       val uiState: StateFlow<MyUiState> = _uiState.asStateFlow()
   }
   ```

5. **Créer les Composants UI** (`ui/components/atoms|molecules|organisms/`)

6. **Créer le Screen** (`ui/screen/`)

   ```kotlin
   @Composable
   fun MyScreen(viewModel: MyViewModel = koinViewModel()) {
       val uiState by viewModel.uiState.collectAsState()
       // UI
   }
   ```

7. **Ajouter au Module Koin** (`di/appModule.kt`)
   ```kotlin
   single<MyService> { MyServiceImpl() }
   single<MyRepository> { MyRepositoryImpl(get()) }
   viewModel { MyViewModel(get()) }
   ```

---

## Conclusion

Cette architecture garantit :

- **Séparation des responsabilités** : Chaque couche a un rôle clair
- **Testabilité** : Facile de tester chaque couche indépendamment
- **Maintenabilité** : Code organisé et facile à comprendre
- **Scalabilité** : Facile d'ajouter de nouvelles fonctionnalités
- **Réutilisabilité** : Composants UI réutilisables grâce au design atomique
