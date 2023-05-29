#function failTest {
#	error "FAV test has failed: $@" 
#	exit 0
#}

# Error level log
error() {
        content="$@" 
	printf "[error]$content\n"
}

failTest() {
        error "FAV test has failed: $@"
        exit 0
}

failTest $@
