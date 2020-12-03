var OcContext = Java.type('net.mycorp.jimin.base.domain.OcContext')

function bindCommands(rootResourceName, resourceName, service) {
	var resource = meta.getResource(resourceName)
	
	for each(var mixinName in resource.mixinList) {
		if(resourceName == mixinName)
			continue
		bindCommands(rootResourceName, mixinName, service)
	}
	
	for each(var command in resource.commands) {
		(function(command) {
			service[command.name] = function(context) {
				if (!(context instanceof OcContext)) {
					if(typeof context == 'undefined') {
						context = new OcContext()
					} else {
						context = new OcContext(context)
					}
				}
				var result = execute(rootResourceName+"."+command.name, context)
				if(command.result == "list")
					result = Java.from(result)
				else if(command.result == "result")
					result.array = Java.from(result.data)
				return result
			}
		})(command)
	}
}

function bindResource(resourceName) {	
	var service = {}
	bindCommands(resourceName, resourceName, service)
	scripts.addBinding(resourceName, service)
}