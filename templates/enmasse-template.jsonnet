local template = import "include/enmasse-template.jsonnet";
local authService = import "include/auth-service.jsonnet";
local kubernetes = import "include/enmasse-kubernetes.jsonnet";
{
    "openshift/enmasse.json": template.generate(false),
    "openshift/addons/keycloak.json": authService.keycloak_openshift,
    "openshift/addons/none-authservice.json": authService.none_openshift,
    "openshift/enmasse-with-kafka.json": template.generate(true),
    "kubernetes/enmasse.json": kubernetes.common(false),
    "kubernetes/addons/external-lb.json": kubernetes.external_lb,
    "kubernetes/addons/keycloak.json": authService.keycloak_kubernetes,
    "kubernetes/addons/none-authservice.json": authService.none_kubernetes
}
