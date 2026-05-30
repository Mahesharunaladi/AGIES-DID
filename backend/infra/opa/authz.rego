package agies.authz

default decision := {
  "allow": false,
  "allowedTools": [],
  "strippedPermissions": ["write", "admin", "network"],
  "reason": "default deny"
}

decision := {
  "allow": true,
  "allowedTools": input.requestedTools,
  "strippedPermissions": [],
  "reason": "high trust workload"
} {
  input.trustBand == "HIGH"
}

decision := {
  "allow": true,
  "allowedTools": read_only_tools,
  "strippedPermissions": ["write", "admin"],
  "reason": "medium trust workload stripped write/admin permissions"
} {
  input.trustBand == "MEDIUM"
  read_only_tools := [tool | tool := input.requestedTools[_]; not contains(lower(tool), "write"); not contains(lower(tool), "admin")]
}

decision := {
  "allow": false,
  "allowedTools": [],
  "strippedPermissions": ["write", "admin", "network"],
  "reason": "low trust workload isolated"
} {
  input.trustBand == "LOW"
}
