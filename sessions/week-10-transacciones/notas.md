## Comparativa de propagaciones

La prueba `nestedAuditIsRolledBackWithOuterTransaction` usa `TransactionTemplate` con `PROPAGATION_NESTED` y un gestor JDBC que soporta savepoints. Verifica que el log existe al terminar el bloque anidado, pero desaparece cuando la transacción externa hace rollback; los saldos también se restauran. Se usa JDBC para demostrar esta semántica sin depender del soporte de savepoints de JPA.

`NESTED` crea un savepoint dentro de la misma transacción física, no un commit independiente. `REQUIRES_NEW` suspende la transacción externa y confirma el log en otra transacción, por lo que este sobrevive al rollback de la transferencia, como demuestra `auditSurvivesTransferRollback`.

Para este requisito la opción adecuada es `REQUIRES_NEW`, no `REQUIRED`: esta última se une a la transacción externa y también perdería el log al revertirse.
