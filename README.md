UltraFinder

## Mode Option
'''javascript
["KEYWORD","FILESIZE"]
'''


## config json template (Standalone Mode)
```javascript
{
	"ultraFinderMode": "KEYWORD",
	"root_paths": [
		"/Users/janux/Desktop"
	],
	"gui_mode": true,
	"detail_mode": true,
	"thread_num": 16,
	"filter": [
		".java",
		".txt",
		".py"
	],
	"keywords": [
		"where proc like \'%s\'"
	],
	"search_options": {
		"case_sensitive": false,
		"top_size_count": 10,
		"min_check_size": 9128
	},
	"demo_mode": false
}
```
## config json template (Server Mode)
```javascript
{
  "ultraFinderMode": "KEYWORD",
  "root_paths": [
    "/Users/janux/Desktop"
  ],
  "filter": [
    ".txt",
    ".java"
  ],
  "keywords": [
    "Hydra"
  ],
  "thread_num": 16,
  "gui_mode": true,
  "detail_mode": true,
  "search_caseSensitive": false,
  "demo_mode": true,
  "top_size_count": 100,
  "min_check_size": 1024
}

```
