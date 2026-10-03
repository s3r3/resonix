# Release optimization rules
# Libraries supply their own consumer rules; keep app-specific rules here.

# Optional APIs referenced by transitive libraries that Android never loads:
# javax.el comes from mbassy (Koin event dispatcher) expression filters.
-dontwarn javax.el.**

# org.ietf.jgss comes from smbj's SPNEGO/Kerberos authenticator, which needs a
# JDK GSSAPI implementation that Android does not provide.
-dontwarn org.ietf.jgss.**