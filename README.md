# hello-openshift

Application Spring Boot minimaliste ("Hello World") prête à être déployée sur OpenShift.

## Structure

```
hello-openshift/
├── pom.xml
├── Dockerfile
├── src/main/java/com/example/hello/
│   ├── HelloOpenshiftApplication.java
│   └── HelloController.java
├── src/main/resources/application.properties
└── openshift/openshift.yaml   # ImageStream, BuildConfig, Deployment, Service, Route
```

## Endpoints

- `GET /` → texte "Hello World"
- `GET /api/hello` → JSON `{ "message": "Hello World", "hostname": "..." }`
- `GET /actuator/health` → santé de l'application (utilisé par les probes)

## Tester en local

```bash
mvn spring-boot:run
# puis
curl http://localhost:8080/
```


## Tester dans docker

  docker build -t hello-openshift:latest .
  docker run -p 8080:8080 hello-openshift:latest

## Déployer sur OpenShift

Deux façons de faire, du plus simple au plus "GitOps".

### Option A — Build binaire directement depuis votre poste (le plus rapide)

Cette option construit l'image directement à partir des sources locales, sans passer par un dépôt Git.

```bash
# 1. Se connecter et se placer dans le bon projet/namespace
oc login <votre-cluster>
oc project <votre-namespace>

# 2. Créer les objets OpenShift (ImageStream, BuildConfig, Deployment, Service, Route)
oc apply -f openshift/openshift.yaml

# 3. Adapter l'image de reference dans le Deployment au namespace courant
oc set image deployment/hello-openshift \
  hello-openshift=image-registry.openshift-image-registry.svc:5000/$(oc project -q)/hello-openshift:latest

# 4. Lancer le build en envoyant le contenu du dossier courant (contexte Docker)
oc start-build hello-openshift --from-dir=. --follow

# 5. Récupérer l'URL publique
oc get route hello-openshift
```

À chaque modification du code, relancez simplement `oc start-build hello-openshift --from-dir=. --follow` :
OpenShift reconstruit l'image et le déploiement se met à jour automatiquement (grâce au trigger d'ImageStream).

### Option B — Build à partir d'un dépôt Git

Si le projet est poussé sur Git (GitHub, GitLab, Gitea interne, etc.), remplacez dans `openshift.yaml` :

```yaml
spec:
  source:
    type: Git
    git:
      uri: https://github.com/votre-org/hello-openshift.git
      ref: main
```

puis :

```bash
oc apply -f openshift/openshift.yaml
oc start-build hello-openshift --follow
```

## Notes

- Le `Dockerfile` utilise les images Red Hat UBI OpenJDK 17, compatibles avec les restrictions de sécurité par défaut d'OpenShift (exécution avec un UID arbitraire, groupe root).
- Les probes de readiness/liveness pointent vers les endpoints Actuator dédiés (`/actuator/health/readiness` et `/actuator/health/liveness`).
- Pensez à remplacer `CHANGE_ME_NAMESPACE` dans `openshift/openshift.yaml` si vous n'utilisez pas la commande `oc set image` de l'option A.
