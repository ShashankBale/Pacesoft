#ifndef HTTPRESPONSEPARSER_H
#define HTTPRESPONSEPARSER_H

#ifndef __cplusplus
#include <stdbool.h>
#endif

typedef struct _Header
{
	char* name;
	char* value;
	struct _Header* next;
} Header;


typedef struct _Response
{
	unsigned int statusCode;
	char* status;
	Header* headers;
	char* body;
	bool hasBody;
} Response;

Response* ParseResponse(const char* rawResponse, bool* result);
void FreeHeaders(Header* header);
void FreeResponse(Response* response);
#endif