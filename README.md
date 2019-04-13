UltraFinder


## config json template (Standalone Mode)
```javascript
{
	"ultraFinderMode": "FILESIZE",
	"root_paths": [
		"/Users/janux/Desktop"
	],
	"gui_mode": true,
	"detail_mode": false,
	"thread_num": 16,
	"filter": [
	
	],
	"keywords": [
		"Hydra"
	],
	"search_options": {
		"case_sensitive": false,
		"content_search": true,
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
  "detail_mode": false,
  "search_caseSensitive": false,
  "content_search": false,
  "demo_mode": true,
  "top_size_count": 100,
  "min_check_size": 1024
}

```
