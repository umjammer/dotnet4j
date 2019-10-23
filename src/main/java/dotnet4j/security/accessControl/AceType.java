
package dotnet4j.security.accessControl;

public enum AceType {
    AccessAllowed,
    AccessDenied,
    SystemAudit,
    SystemAlarm,
    AccessAllowedCompound,
    AccessAllowedObject,
    AccessDeniedObject,
    SystemAuditObject,
    SystemAlarmObject,
    AccessAllowedCallback,
    AccessDeniedCallback,
    AccessAllowedCallbackObject,
    AccessDeniedCallbackObject,
    SystemAuditCallback,
    SystemAlarmCallback,
    SystemAuditCallbackObject,
    SystemAlarmCallbackObject,
    MaxDefinedAceType;
}
