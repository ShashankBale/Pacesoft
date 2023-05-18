#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include "httpresponseparser.h"

#define SPACE " "
#define CRLF "\r\n"
#define NULL_TERMINATOR 1

typedef enum _State
{
	ChunkSize,
	ChunkSizeNewLine,
	ChunkSizeNewLine_2,
	ChunkSizeNewLine_3,
	ChunkDataNewLine_1,
	ChunkDataNewLine_2,
	ChunkData,
} State;


Response* ParseResponse(const char* rawResponse, bool* result)
{
	*result = false;
	Response* response = NULL;
	response = (Response*)malloc(sizeof(Response));

	if (!response)
	{
		*result = false;
		return NULL;
	}

	memset(response, 0, sizeof(Response));
	response->hasBody = true;
	size_t httpVersionLength = strcspn(rawResponse, SPACE);
	rawResponse += httpVersionLength + strlen(SPACE);
	size_t statusCodeLength = strcspn(rawResponse, SPACE);
	char* statusCodeStr = (char*)malloc(statusCodeLength + NULL_TERMINATOR);
	if (statusCodeStr)
	{
		memcpy((void*)statusCodeStr, (void*)rawResponse, statusCodeLength);
		statusCodeStr[statusCodeLength] = '\0';
		int result = atoi(statusCodeStr);
		if (result != 0)
		{
			response->statusCode = result;
		}
		free(statusCodeStr);
		statusCodeStr = NULL;
	}
	rawResponse = rawResponse + statusCodeLength + strlen(SPACE);
	size_t statusLength = strcspn(rawResponse, CRLF);
	response->status = (char*)malloc(statusLength + NULL_TERMINATOR);

	if (response->status)
	{
		memcpy((void*)response->status, (void*)rawResponse, statusLength);
		response->status[statusLength] = '\0';
	}
	rawResponse = rawResponse + statusLength + strlen(CRLF);

	Header* header = NULL, * last = NULL;
	while (rawResponse[0] != '\r' || rawResponse[1] != '\n')
	{
		last = header;
		header = (Header*)malloc(sizeof(Header));
		if (header)
		{
			// name
			size_t name_len = strcspn(rawResponse, ":");
			header->name = (char*)malloc(name_len + 1);
			if (header->name)
			{
				memcpy(header->name, rawResponse, name_len);
				header->name[name_len] = '\0';
				rawResponse += name_len + 1; // move past :

				while (*rawResponse == ' ')
				{
					rawResponse++;
				}

				// value
				size_t value_len = strcspn(rawResponse, CRLF);
				header->value = (char*)malloc(value_len + 1);
				if (header->value)
				{
					memcpy(header->value, rawResponse, value_len);
					header->value[value_len] = '\0';
					rawResponse = rawResponse + value_len + strlen(CRLF); // move past <CR><LF>
					header->next = last;
				}
			}
		}
	}
	response->headers = header;
	rawResponse = rawResponse + strlen(CRLF);

	if (rawResponse[0] == '\0')
	{
		response->hasBody = false;
		*result = true;
	}
	else
	{
		bool isChunked = false;
		if (response->headers)
		{
			Header* node = response->headers;
			while (node)
			{
				if (node->name && node->value)
				{
					if (strstr(node->name, "Transfer-Encoding") && strstr(node->value, "chunked"))
					{
						isChunked = true;
						break;
					}
				}
				node = node->next;
			}
		}

		size_t responseBodyLength = strlen(rawResponse);
		response->body = (char*)malloc(sizeof(char) * (responseBodyLength + NULL_TERMINATOR));

		if (!isChunked)
		{
			if (response->body)
			{
				memcpy(response->body, rawResponse, responseBodyLength);
				response->body[responseBodyLength] = '\0';
				*result = true;
			}
		}
		else
		{
			const char* begin = rawResponse;
			const char* end = rawResponse + strlen(rawResponse);
			State state = ChunkSize;
			char chunkSizeStr[20] = { '\0' };
			size_t chunkSize = 0;
			int counter = 0;
			while (begin != end)
			{
				char input = *begin++;
				switch (state)
				{
				case ChunkSize:
					if (isalnum(input))
					{
						strncat(chunkSizeStr, &input, 1);
					}
					else if (input == '\r')
					{
						state = ChunkSizeNewLine;
					}
					break;
				case ChunkSizeNewLine:
					if (input == '\n')
					{
						chunkSize = strtol(chunkSizeStr, NULL, 16);
						memset(chunkSizeStr, 0, sizeof(chunkSizeStr));

						if (chunkSize == 0)
							state = ChunkSizeNewLine_2;
						else
							state = ChunkData;
					}
					break;
				case ChunkSizeNewLine_2:
					if (input == '\r')
					{
						state = ChunkSizeNewLine_3;
					}
					break;
				case ChunkSizeNewLine_3:
					if (input == '\n')
					{
						*result = true;
					}
					break;
				case ChunkData:
					if (response->body && counter < responseBodyLength)
					{
						response->body[counter] = input;
						response->body[counter + 1] = '\0';
						counter++;
					}
					if (--chunkSize == 0)
					{
						state = ChunkDataNewLine_1;
					}
					break;
				case ChunkDataNewLine_1:
					if (input == '\r')
					{
						state = ChunkDataNewLine_2;
					}
					break;
				case ChunkDataNewLine_2:
					if (input == '\n')
					{
						state = ChunkSize;
					}
					break;
				default:
					break;
				}
			}
		}
	}

	return response;
}

void FreeHeaders(Header* header)
{
	if (header)
	{
		if (header->name)
			free(header->name);
		if (header->value)
			free(header->value);
		if (header->next)
			FreeHeaders(header->next);
		free(header);
	}
}


void FreeResponse(Response* response)
{
	if (response)
	{
		if (response->status)
			free(response->status);
		if (response->headers)
			FreeHeaders(response->headers);
		if (response->body)
			free(response->body);
		free(response);
	}
}
