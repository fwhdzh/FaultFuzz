function failTest {
	error "FAV test has failed: $@" 
	exit 0
}

# Error level log.
function error {
        content="$@" 
	printf "[error]$content\n"
}

failTest $@
